package com.ayala.dam.firebasenotesfragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayala.dam.firebasenotes.Nota
import kotlinx.android.synthetic.main.fragment_datos.view.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ayala.dam.firebasenotesfragments.R.id.parent
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog.view.*


class DatosAdapterNotas(val activity: AppCompatActivity, val items: MutableList<Nota>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    // Obtiene el número de datos
    override fun getItemCount(): Int {
        return items.size
    }

    //infla el layout activity_datos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_datos, parent, false))
    }

    // carga datos del ArrayList aL TEXTVIEW view
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.tvDatosB?.text = items.get(position).id
        holder?.tvDatosA?.text = items.get(position).title
        if (position % 2 == 0)
            holder?.tvDatosA?.setBackgroundColor(Color.parseColor("#D81B60"))
        else
            holder?.tvDatosA?.setBackgroundColor(Color.parseColor("#008577"))
        holder?.tvDatosA?.setOnClickListener {
            val fragmentManager = activity.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content_main, FragmentRegister.newInstance(items.get(position).note), "rageComicList")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        holder?.tvDatosA?.setOnLongClickListener {
            Log.d("FIREBASE-NOTES", items.get(position).id)
            showDialog(items.get(position).id)
            true
        }
    }

    fun message(msg: String) {
        Snackbar.make(activity!!.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }

    fun showDialog(id:String) {
        var builder: AlertDialog.Builder = AlertDialog.Builder(activity);
        var inflater: LayoutInflater = LayoutInflater.from(context)
        var view: View = inflater.inflate(R.layout.dialog_delete, activity!!.findViewById(android.R.id.content),false)
        builder.setView(view)
        builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                message("Canceled")
                dialog!!.dismiss()
            }
        })
        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var dbReference: DatabaseReference
                var database: FirebaseDatabase
                database = FirebaseDatabase.getInstance()
                dbReference = database.getReference("notas")
                dbReference.child(MainActivity.prefs.token).child(id).removeValue()
                message("Nota borrada correctamente")
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Mantiene el TextView
    val tvDatosA = view.textViewTitulo
    val tvDatosB = view.textViewId
    val view = view
}
