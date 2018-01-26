package com.gentics.elasticsearch;

import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.Wait;

import com.gentics.elasticsearch.client.ElasticsearchOkClient;
import com.gentics.elasticsearch.client.HttpErrorException;

import io.vertx.core.json.JsonObject;

public class IndexMethodsTest {

	@ClassRule
	public static GenericContainer<?> elasticsearch = new GenericContainer<>("docker.elastic.co/elasticsearch/elasticsearch:6.1.2").withEnv(
			"discovery.type", "single-node").withExposedPorts(9200).waitingFor(Wait.forHttp("/"));

	@Test
	public void testIndexCreate() throws HttpErrorException {
		ElasticsearchOkClient<JsonObject> client = new ElasticsearchOkClient<>("http", "localhost", elasticsearch.getMappedPort(9200));
		client.setConverterFunction(JsonObject::new);

		JsonObject response = client.createIndex("blub", new JsonObject());
		assertTrue(response.getBoolean("acknowledged"));

		JsonObject response2 = client.deleteIndex("blub");
		assertTrue(response2.getBoolean("acknowledged"));
	}
}
