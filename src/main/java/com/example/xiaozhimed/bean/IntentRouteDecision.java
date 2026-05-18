package com.example.xiaozhimed.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentRouteDecision {

    private String route;
    private Double confidence;
    private String reason;
}
