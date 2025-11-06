package com.mfhe.dto;

import java.util.List;

public record CombinedTimeSeries(List<TimeSeriesPoint> housingStarts, List<TimeSeriesPoint> employment) {
}
