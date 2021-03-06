package com.nivethsaran.spaceshipar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private val modelResourceIds= arrayOf(
        R.raw.star_destroyer,
        R.raw.tie_silencer,
        R.raw.xwing
    )
    private var curCameraPosition = Vector3.zero()
    private val nodes= mutableListOf<RotatingNode>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment= fragment as ArFragment

        arFragment.setOnTapArPlaneListener{hitResult,plane, motionEvent->
            val randomId= modelResourceIds.random()
            loadModelandAddToScene(hitResult.createAnchor(),randomId)
        }

        arFragment.arSceneView.scene.addOnUpdateListener{
            updateNodes()
        }
    }

    private fun updateNodes(){
        curCameraPosition=arFragment.arSceneView.scene.camera.worldPosition
        for(node in nodes)
        {
            node.worldPosition= Vector3(curCameraPosition.x,node.worldPosition.y,curCameraPosition.z)
        }
    }

    private fun loadModelandAddToScene(anchor: Anchor,modelResourceId: Int)
    {
        ModelRenderable.builder()
            .setSource(this,modelResourceId)
            .build()
            .thenAccept{
                val spaceship = when(modelResourceId){
                    R.raw.star_destroyer -> Spaceship.StarDestroyer
                    R.raw.tie_silencer -> Spaceship.TieSilencer
                    R.raw.xwing -> Spaceship.Xwing
                    else -> Spaceship.Xwing
                }
                addNodeToScene(anchor,it,spaceship)
            }.exceptionally {
                Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun addNodeToScene(anchor: Anchor,modelRenderable: ModelRenderable, spaceship: Spaceship){
        val anchorNode= AnchorNode(anchor)
        val rotatingNode=RotatingNode(spaceship.degreesPerSecond).apply {
            setParent(anchorNode)
        }
        Node().apply {
            renderable= modelRenderable
            setParent(rotatingNode)
            localPosition = Vector3(spaceship.radius,spaceship.height, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f,spaceship.rotationDegrees,0f))

        }
        arFragment.arSceneView.scene.addChild(anchorNode)
        nodes.add(rotatingNode)
    }
}