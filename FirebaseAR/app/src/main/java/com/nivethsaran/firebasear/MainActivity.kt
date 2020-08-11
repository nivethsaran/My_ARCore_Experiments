package com.nivethsaran.firebasear

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val url = "https://firebasestorage.googleapis.com/v0/b/ar-sceneform-experiment.appspot.com/o/model.glb?alt=media&token=86173eff-c2f4-4e4c-ac8d-d36785db1dcb"
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            spawnObject(hitResult.createAnchor(), Uri.parse(url))
        }
    }

    private fun spawnObject(anchor: Anchor, modelUri: Uri) {
        val renderableSource = RenderableSource.builder()
                .setSource(this, modelUri, RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()
        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(modelUri)
                .build()
                .thenAccept {
                    addNodeToScene(anchor, it)
                }.exceptionally {
                    Log.e("MainActivity", "Error loading model", it)
                    null
                }
    }

    private fun addNodeToScene(anchor: Anchor, modelRenderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            setParent(anchorNode)
        }
        arFragment.arSceneView.scene.addChild(anchorNode)
    }
}