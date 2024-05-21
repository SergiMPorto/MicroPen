package com.sergi.micropen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sergi.micropen.idioma.Idioma
class LanguageAdapter(
    private val context: Context,
    private val languages: List<Idioma>,
    private val downloadedLanguages: MutableSet<String>
) : ArrayAdapter<Idioma>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.language_item, parent, false)
        val languageTitle = view.findViewById<TextView>(R.id.language_name)
        val downloadIcon = view.findViewById<ImageView>(R.id.language_icon)

        val language = getItem(position)
        languageTitle.text = language?.languageTitle ?: "Unknown"

        // Mostrar u ocultar el icono de descarga
        if (language != null && downloadedLanguages.contains(language.languageCode)) {
            downloadIcon.visibility = View.GONE
        } else {
            downloadIcon.visibility = View.VISIBLE
        }

        return view
    }
}