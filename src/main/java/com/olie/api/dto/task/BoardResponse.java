package com.olie.api.dto.task;

import java.util.List;

public record BoardResponse(List<TaskResponse> todo, List<TaskResponse> doing, List<TaskResponse> done) {
}
