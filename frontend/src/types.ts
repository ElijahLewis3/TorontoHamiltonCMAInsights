export interface Region {
  id: number;
  name: string;
  code: string;
}

export interface TimeSeriesPoint {
  date: string;
  value: number;
}

export interface CombinedTimeSeries {
  housingStarts: TimeSeriesPoint[];
  employment: TimeSeriesPoint[];
}

export interface NaicsShare {
  industry: string;
  value: number;
}

export interface HousingCategorySummary {
  category: string;
  starts: number;
  completions: number;
}

export interface HeatmapCell {
  period: string;
  category: string;
  value: number;
}
