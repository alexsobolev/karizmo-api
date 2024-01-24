package com.axelspringer.karizmo.controller

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.FieldValueList
import com.google.cloud.bigquery.JobId
import com.google.cloud.bigquery.JobInfo
import com.google.cloud.bigquery.QueryJobConfiguration
import com.google.cloud.bigquery.TableResult
import com.google.cloud.vertexai.VertexAI
import com.google.cloud.vertexai.api.GenerationConfig
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel
import org.springframework.boot.autoconfigure.batch.BatchProperties.Job
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Controller
class MainController {

  @PostMapping("/")
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
      //exception.toString()
      ""
    }

    return ResponseEntity.ok(hashMapOf("response" to response))
  }

  @GetMapping("/headlines")
  fun headlines(): ResponseEntity<MutableList<Map<String, String>>> {
    val bigquery: BigQuery = BigQueryOptions.getDefaultInstance().getService()
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration.newBuilder(
      "SELECT cms_id, headline FROM `aspringer-hackathon24ber-6081.hackathon.articles` LIMIT 50"
    ) // Use standard SQL syntax for queries.
      // See: https://cloud.google.com/bigquery/sql-reference/
      .setUseLegacySql(false)
      .build()

    // Create a job ID so that we can safely retry.

    // Create a job ID so that we can safely retry.
    val jobId: JobId = JobId.of(UUID.randomUUID().toString())
    var queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.

    // Wait for the query to complete.
    queryJob = queryJob.waitFor()

    // Check for errors

    // Check for errors
    if (queryJob == null) {
      throw RuntimeException("Job no longer exists")
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw RuntimeException(queryJob.getStatus().getError().toString())
    }

    // Get the results.

    // Get the results.
    val result: TableResult = queryJob.getQueryResults()

    // Print all pages of the results.

    val response = mutableListOf<Map<String, String>>()
    // Print all pages of the results.
    for (row: FieldValueList in result.iterateAll()) {
      response.add(
        hashMapOf(
          "cms_id" to row.get("cms_id").getStringValue(),
          "headline" to row.get("headline").getStringValue()
        )
      )
    }

    return ResponseEntity.ok(response)
  }

  @GetMapping("/articles/{id}")
  fun article(@PathVariable("id") id: Int): ResponseEntity<HashMap<String, HashMap<String, String>?>> {
    val bigquery: BigQuery = BigQueryOptions.getDefaultInstance().getService()
    val queryConfig: QueryJobConfiguration = QueryJobConfiguration.newBuilder(
      "SELECT cms_id, headline, full_text  FROM `aspringer-hackathon24ber-6081.hackathon.articles` AS articles WHERE articles.cms_id = $id LIMIT 1"
    ) // Use standard SQL syntax for queries.
      // See: https://cloud.google.com/bigquery/sql-reference/
      .setUseLegacySql(false)
      .build()

    // Create a job ID so that we can safely retry.

    // Create a job ID so that we can safely retry.
    val jobId: JobId = JobId.of(UUID.randomUUID().toString())
    var queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build())

    // Wait for the query to complete.

    // Wait for the query to complete.
    queryJob = queryJob.waitFor()

    // Check for errors

    // Check for errors
    if (queryJob == null) {
      throw RuntimeException("Job no longer exists")
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw RuntimeException(queryJob.getStatus().getError().toString())
    }

    // Get the results.

    // Get the results.
    val result: TableResult = queryJob.getQueryResults()

    // Print all pages of the results.

    return ResponseEntity.ok(hashMapOf(
      "article" to result.values.firstOrNull()?.let {
        hashMapOf(
          "cms_id" to it.get("cms_id").getStringValue(),
          "headline" to it.get("headline").getStringValue(),
          "full_text" to it.get("full_text").getStringValue()
        )
      }
    ))
  }
}