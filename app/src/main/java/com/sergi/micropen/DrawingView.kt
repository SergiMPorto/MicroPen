package com.sergi.micropen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.digitalink.Ink

//La clase desciende de View
class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val path = Path()   //Para guardar trazos de un usuario.
    private val paint = Paint().apply {  //Configura como se dibujan los trazos
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }
    private val bitmap: Bitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888) //La imagen que capturamos en la memoria. Es un matriz en píxeles en pantalla.
    private val canvas: Canvas = Canvas(bitmap) // Es donde se dibuja el Path Permanente.
    private val strokeList = mutableListOf<MutableList<Ink.Point>>()  //Almacena los puntos de trazos.

    init {
        setBackgroundColor(Color.WHITE)  // Fondo de la pantalla para escribir
    }
// Dibuja el contenido del Bitmap
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.drawPath(path, paint)
    }

    //Manejar los eventos táctiles.
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            //Iniciar trazo
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                strokeList.add(mutableListOf(Ink.Point.create(x, y)))
                return true
            }//Continuar trazos
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                strokeList[strokeList.lastIndex].add(Ink.Point.create(x, y))
                invalidate()
            }//Finalizar el trazo lo dibuja en el Canvas.
            MotionEvent.ACTION_UP -> {
                canvas.drawPath(path, paint)
                path.reset()
            }
            else -> return false
        }
        invalidate()
        return true
    }
//Para convertir los puntos capturados  en objeto Ink.Stroke
    fun getStrokes(): List<Ink.Stroke> {
        return strokeList.map { strokePoints ->
            val strokeBuilder = Ink.Stroke.builder()
            strokePoints.forEach { point ->
                strokeBuilder.addPoint(point)
            }
            strokeBuilder.build()
        }
    }

    //Añadí un método para borrar el contenido.

    fun deleteTrazos(){
        path.reset() //Resetear los puntos que hemos escritos
        strokeList.clear() //limpiar la lista de trazos
        canvas.drawColor(Color.WHITE) // limpiar Canvas y pintarlo del mismo color que tiene
        invalidate() // Le dice al sistema que donde se escribió cambió.
    }
}
