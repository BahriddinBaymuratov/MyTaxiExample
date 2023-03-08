package com.example.mytaxi

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    var mapView: MapView? = null

    // create object for annotation or Marker
    var annotationApi: AnnotationPlugin? = null
    lateinit var annotationConfig: AnnotationConfig
    var layerIDD = "map_annotation" // this is hard code value
    var pointAnnotationManager: PointAnnotationManager? = null

    // Marker list for displaying multiple marker
    var markerList: ArrayList<PointAnnotationOptions> = ArrayList()
    var latitudeList: ArrayList<Double> = ArrayList()
    var longitudeList: ArrayList<Double> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()


        mapView = findViewById(R.id.mapView)
        createDummyList()
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {
                zoomCamera()
                // Now add marker
                annotationApi = mapView?.annotations
                annotationConfig = AnnotationConfig(
                    layerId = layerIDD
                )
                // initialize point annotation manager
                pointAnnotationManager =
                    annotationApi?.createPointAnnotationManager(annotationConfig)
                createMarkerOnMap()
            }
        })
    }

    private fun zoomCamera() {
        mapView!!.getMapboxMap().setCamera(
            // first value should be longitude and then latitude
            CameraOptions.Builder().center(Point.fromLngLat(75.8577, 22.7196))
                .zoom(11.0)
                .build()
        )
    }

    // Create dummy list of lat long
    private fun createDummyList() {
        latitudeList.add(22.7196)
        longitudeList.add(75.8577)

        latitudeList.add(23.1765)
        longitudeList.add(75.7885)

        latitudeList.add(22.9676)
        longitudeList.add(76.0534)
    }

    private fun clearAnnotation() {
        markerList = ArrayList()
        pointAnnotationManager?.deleteAll()
    }

    private fun createMarkerOnMap() {
        // before that we will remove marker
        clearAnnotation()
        // Adding Click event of marker
        pointAnnotationManager?.addClickListener(OnPointAnnotationClickListener { annotation: PointAnnotation ->
            onMarkerIteClick(annotation)
            true
        })

        val bitmap =
            convertDrawableToBitmap(AppCompatResources.getDrawable(this, R.drawable.marker))

        for (i in 0 until 3) {
            val jsonObject = JSONObject()
            jsonObject.put("somevalue", i)

            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitudeList[i], latitudeList[i]))
                .withData(Gson().fromJson(jsonObject.toString(), JsonElement::class.java))
                .withIconImage(bitmap)

            markerList.add(pointAnnotationOptions)
        }
        pointAnnotationManager?.create(markerList)

    }

    // Convert Drawable to bitmap
    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap {
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constatState = sourceDrawable?.constantState
            val drawable = constatState?.newDrawable()?.mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    // We well use marker value later
    private fun onMarkerIteClick(marker: PointAnnotation) {

        val jsonElement: JsonElement? = marker.getData()
        AlertDialog.Builder(this)
            .setTitle("MarkerClick")
            .setMessage("Click" + jsonElement.toString())
            .setPositiveButton("ok") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


}