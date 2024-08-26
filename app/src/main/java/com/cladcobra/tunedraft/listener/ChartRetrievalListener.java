package com.cladcobra.tunedraft.listener;

import com.cladcobra.tunedraft.chart.Hot100Chart;

public interface ChartRetrievalListener {
    void onChartRetrieved(Hot100Chart chart);
}