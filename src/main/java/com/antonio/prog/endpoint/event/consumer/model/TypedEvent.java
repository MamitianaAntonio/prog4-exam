package com.antonio.prog.endpoint.event.consumer.model;

import com.antonio.prog.PojaGenerated;
import com.antonio.prog.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
