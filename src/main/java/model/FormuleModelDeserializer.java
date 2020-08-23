package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import actions.AbstractAction;
import actions.ActionWithContent;
import actions.ActionWithNextCondition;
import constants.ActionType;

public class FormuleModelDeserializer extends JsonDeserializer<FormuleModel> {
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public FormuleModel deserialize(JsonParser jsonParser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		final List<AbstractAction> actionList = new ArrayList<>();

		final ObjectCodec oc = jsonParser.getCodec();
		final JsonNode actionListNode = oc.readTree(jsonParser);
		final ArrayNode node = (ArrayNode) actionListNode.get("actionList");
		final Iterator<JsonNode> actions = node.elements();
		for (; actions.hasNext();) {
			final JsonNode actionNode = actions.next();
			final String actionType = actionNode.get("actionType").asText();

			AbstractAction action;
			if (isActionWithNextCondition(actionType)) {
				action = mapper.readValue(actionNode.toString(), ActionWithNextCondition.class);
			} else if (isActionWithContent(actionType)) {
				action = mapper.readValue(actionNode.toString(), ActionWithContent.class);
			} else {
				throw new RuntimeException("Unknow action type when deserializing : " + actionType);
			}

			actionList.add(action);
		}

		return new FormuleModel(actionList);
	}

	private boolean isActionWithContent(final String actionType) {
		return ActionType.SELECT.name().equalsIgnoreCase(actionType)
				|| ActionType.FILL.name().equalsIgnoreCase(actionType);
	}

	private boolean isActionWithNextCondition(final String actionType) {
		return ActionType.NAVIGATE.name().equalsIgnoreCase(actionType)
				|| ActionType.CLICK.name().equalsIgnoreCase(actionType);
	}
}
