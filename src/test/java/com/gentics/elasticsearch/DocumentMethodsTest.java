package com.gentics.elasticsearch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.Wait;

import com.gentics.elasticsearch.client.HttpErrorException;
import com.gentics.elasticsearch.client.okhttp.ElasticsearchOkClient;

import io.vertx.core.json.JsonObject;

public class DocumentMethodsTest {

	@ClassRule
	public static GenericContainer<?> elasticsearch = new GenericContainer<>("docker.elastic.co/elasticsearch/elasticsearch:6.1.2")
		.withEnv("discovery.type", "single-node")
		.withExposedPorts(9200)
		.waitingFor(Wait.forHttp("/"));

	@Test
	public void testDocumentCreate() throws HttpErrorException {
		ElasticsearchOkClient<JsonObject> client = new ElasticsearchOkClient<>("http", "localhost", elasticsearch.getMappedPort(9200));
		client.setConverterFunction(JsonObject::new);

		client.createIndex("dummy", new JsonObject()).sync();
		client.storeDocument("blub", "default", "one", new JsonObject().put("key1", "value1")).sync();

		JsonObject doc = client.readDocument("blub", "default", "one").sync();
		assertEquals("value1", doc.getJsonObject("_source").getString("key1"));
	}

	@Test
	public void testDocumentCreateAsync() throws IOException {
		ElasticsearchOkClient<JsonObject> client = new ElasticsearchOkClient<>("http", "localhost", elasticsearch.getMappedPort(9200));
		client.setConverterFunction(JsonObject::new);

		JsonObject doc = client.createIndex("dummy", new JsonObject()).async().toCompletable()
			.andThen(client.storeDocument("blub", "default", "one", new JsonObject().put("key1", "value1")).async().toCompletable())
			.andThen(client.readDocument("blub", "default", "one").async()).blockingGet();

		assertEquals("value1", doc.getJsonObject("_source").getString("key1"));
	}
}
