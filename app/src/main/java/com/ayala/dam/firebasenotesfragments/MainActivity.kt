package com.ayala.dam.firebasenotesfragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.*
import android.graphics.BitmapFactory.decodeResource
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.Adapter
import com.ayala.dam.firebasenotes.Nota
import com.google.firebase.database.*

// TODO: FIREBASE LIBRERIAS

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog.view.*
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var prefs: Prefs
    }

    private lateinit var activityAdapter: MainActivity
    private lateinit var adaptador: DatosAdapterNotas

    var notas: MutableList<Nota> = ArrayList()

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase

    private val p = Paint()
    private var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        activityAdapter = this

        recyclerViewFragmentNotes.layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewFragmentNotes.layoutManager = GridLayoutManager(applicationContext, 1)
        adaptador = DatosAdapterNotas(activityAdapter, notas, applicationContext!!)
        recyclerViewFragmentNotes.adapter = adaptador

        fab.setOnClickListener { showDialog() }

        // TODO: Asignamos las preferencias, en este caso cargar/guardar los tokens
        prefs = Prefs(applicationContext)

        if (MainActivity.prefs.token.equals(""))
        {
            MainActivity.prefs.token = (Random().nextInt((999999999 + 1) - 1) + 1).toString()
        }


        firebaseLoad()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun message(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }


    fun showDialog() {
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.dialog, null)
        builder.setView(view)
        builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                message("Canceled")
                dialog!!.dismiss()
            }
        })
        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                firebaseNewContent(view.et_title.text.toString(), view.et_note.text.toString())
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (direction == ItemTouchHelper.LEFT) {
                    adaptador!!.removeItem(position)
                } else {

                    adaptador!!.removeItem(position)

                }
            }

            private fun removeView() {
                if (view!!.parent != null) {
                    (view!!.parent as ViewGroup).removeView(view)
                }
            }

            @SuppressLint("PrivateResource")
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX > 0) {
                        p.color = Color.parseColor("#388E3C")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = decodeResource(resources, R.drawable.abc_ic_menu_copy_mtrl_am_alpha)
                        val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    } else {
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = decodeResource(resources, R.drawable.abc_ic_menu_cut_mtrl_alpha)
                        val icon_dest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerViewFragmentNotes)
    }

    fun firebaseLoad() {
        notas.clear()
        notas.add(Nota("1", "Cargando notas", "Cargando notas"))
        database = FirebaseDatabase.getInstance()
        dbReference = database.getReference("notas")
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FIREBASE-NOTAS", "Cargando datos")
                notas.clear()
//                val gson= Gson()
                for (item in dataSnapshot.children) {
                    try {
                        val registro = JSONObject(item.getValue().toString())
                        val titulo = registro["title"].toString().replace("_", " ")
                        val descripcion = registro["note"].toString().replace("_", " ")
                        notas.add(Nota(item.key!!, titulo, descripcion))
                    } catch (e: Exception) {
                        Log.d("FIREBASE-NOTAS", e.message.toString())
                    }
                }
                if (notas.size == 0) {
                    notas.add(Nota("1", "No se ha encontrado ninguna nota", "No se ha encontrado ninguna nota"))
                    message("No se ha encontrado ninguna nota")
                }
                Log.d("FIREBASE-NOTAS", notas.toString())
                recyclerViewFragmentNotes.adapter = DatosAdapterNotas(activityAdapter, notas, applicationContext!!)
//                initSwipe()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }
        dbReference.child( MainActivity.prefs.token).addValueEventListener(menuListener)
    }

    fun firebaseNewContent(title: String, note: String) {
        try {
            val titulo = title.replace(" ", "_")
            val descripcion = note.replace(" ", "_")
            var nota = Nota((Random().nextInt((999999999 + 1) - 1) + 1).toString(), titulo, descripcion)
            dbReference.child( MainActivity.prefs.token).push().setValue(nota)
            message("Nota guardada con éxito")
        } catch (e: Exception) {
            message(e.message.toString())
        }

    }
}
