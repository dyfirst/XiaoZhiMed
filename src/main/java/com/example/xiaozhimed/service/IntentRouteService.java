package com.example.xiaozhimed.service;

import com.example.xiaozhimed.bean.IntentRouteDecision;

public interface IntentRouteService {

    IntentRouteDecision route(Long memberId, String message);
}
