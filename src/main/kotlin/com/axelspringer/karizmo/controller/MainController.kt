package com.axelspringer.karizmo.controller

import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.Content
import com.google.cloud.vertexai.api.GenerateContentResponse
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.api.Part
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel
import com.google.cloud.vertexai.generativeai.preview.ResponseStream
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class MainController {

  @PostMapping("/", )
  fun index(@RequestBody prompt: String): ResponseEntity<HashMap<String, String>> {
    val response = try {
      VertexAI("aspringer-hackathon24ber-6081", "us-central1").use { vertexAi ->
        val generationConfig: GenerationConfig =
          GenerationConfig.newBuilder().setMaxOutputTokens(8192).setTemperature(0.9f).setTopP(1F).build()
        val generativeModel = GenerativeModel("gemini-pro", generationConfig, vertexAi)

        generativeModel.generateContent(prompt)
          .getCandidates(0).content.getParts(0).text
      }
    } catch (exception: Exception) {
      println(exception)
      exception.toString()
    }

    return ResponseEntity.ok(hashMapOf("response" to response))
  }
}