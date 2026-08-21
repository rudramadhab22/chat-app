package com.rudra.ed.websocket;

import tools.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WsEnvelope {

	private WsMessageType type;
	private JsonNode payload;
}
