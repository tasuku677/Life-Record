package com.example.mylife

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


##一覧画面
class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        realm=Realm.getDefaultInstance()
        list.layoutManager= LinearLayoutManager(this)
        val schedules=realm.where<Schedule>().findAll()
        val adapter=ScheduleAdapter(schedules)
        list.adapter=adapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val intent= Intent(this,ScheduleEditActivity::class.java)
            startActivity(intent)
        }
        adapter.setOnItemClickListener { id ->
            val intent= Intent(this,ScheduleEditActivity::class.java)
                .putExtra("schedule_id",id)
            startActivity(intent)
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        realm.close()
    }

}


## 登録画面, 訂正画面
package com.example.mylife

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.format.DateFormat
import android.view.View
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_schedule_edit.*

class ScheduleEditActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_edit)
        realm = Realm.getDefaultInstance()

        val scheduleId = intent?.getLongExtra("schedule_id", -1L)
        if (scheduleId != -1L) {
            val schedule = realm.where<Schedule>()
                .equalTo("id", scheduleId).findFirst()
            typeEdit.setText(schedule?.type)
            titleEdit.setText(schedule?.title)
            delete.visibility = View.VISIBLE
        } else {
            delete.visibility = View.INVISIBLE
        }


        save.setOnClickListener { view: View ->
            if (scheduleId == -1L) {
                realm.executeTransaction { db: Realm ->
                    val maxId = db.where<Schedule>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1
                    val schedule = db.createObject<Schedule>(nextId)
                    schedule.title = titleEdit.text.toString()
                    schedule.type = typeEdit.text.toString()
                }
                Snackbar.make(view, "追加しました", Snackbar.LENGTH_LONG)
                    .setAction("戻る") { finish() }
                    .setActionTextColor(Color.YELLOW)
                    .show()
            } else if (scheduleId != -1L) {
                realm.executeTransaction { db: Realm ->
                    val schedule = db.where<Schedule>()
                        .equalTo("id", scheduleId).findFirst()
                    schedule?.title = titleEdit.text.toString()
                    schedule?.type = typeEdit.text.toString()
                }
                Snackbar.make(view, "修正しました", Snackbar.LENGTH_LONG)
                    .setAction("戻る") { finish() }
                    .setActionTextColor(Color.RED)
                    .show()
            }
        }
        delete.setOnClickListener { view: View ->
            realm.executeTransaction { db: Realm ->
                db.where<Schedule>().equalTo("id", scheduleId)
                    ?.findFirst()
                    ?.deleteFromRealm()
            }
            Snackbar.make(view, "削除しました", Snackbar.LENGTH_LONG)
                .setAction("戻る") { finish() }
                .setActionTextColor(Color.BLUE)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}


package com.example.mylife

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class ScheduleAdapter(data: OrderedRealmCollection<Schedule>):
    RealmRecyclerViewAdapter<Schedule,ScheduleAdapter.ViewHolder>(data,true) {
    private var listener:((Long?)->Unit)?=null
    fun setOnItemClickListener(listener:(Long?)->Unit){
        this.listener=listener
    }
    init{
        setHasStableIds(true)
    }
    class ViewHolder(cell: View): RecyclerView.ViewHolder(cell){
        val type: TextView =cell.findViewById(android.R.id.text1)
        val title: TextView =cell.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ScheduleAdapter.ViewHolder {
        val inflater= LayoutInflater.from(p0.context)
        val view=inflater.inflate(android.R.layout.simple_list_item_2,p0,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ScheduleAdapter.ViewHolder, p1: Int) {
        val schedule:Schedule?=getItem(p1)
        p0.type.text=schedule?.type
        p0.title.text=schedule?.title
        p0.itemView.setOnClickListener{
            listener?.invoke(schedule?.id)
        }
    }
    override fun getItemId(p0:Int):Long{
        return getItem(p0)?.id?:0
    }

}
