import type {
  Region,
  CombinedTimeSeries,
  NaicsShare,
  HousingCategorySummary,
  HeatmapCell,
} from './types';

async function get<T>(url: string): Promise<T> {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json() as Promise<T>;
}

export const fetchRegions = () => get<Region[]>('/api/regions');

export const fetchCombined = (code: string) =>
  get<CombinedTimeSeries>(`/api/insights/combined/${code}`);

export const fetchNaics = (code: string) =>
  get<NaicsShare[]>(`/api/insights/employment/naics/${code}`);

export const fetchHousingCategories = (code: string) =>
  get<HousingCategorySummary[]>(`/api/insights/housing/categories/${code}`);

export const fetchHeatmap = (code: string) =>
  get<HeatmapCell[]>(`/api/insights/housing/heatmap/${code}`);

export async function refreshData(apiKey?: string): Promise<void> {
  const headers: Record<string, string> = {};
  if (apiKey) headers['X-API-Key'] = apiKey;
  const res = await fetch('/api/admin/refresh', { method: 'POST', headers });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
}

export const exportEmployment = (code: string) =>
  window.open(`/api/export/employment/${code}`, '_blank');

export const exportHousing = (code: string) =>
  window.open(`/api/export/housing/${code}`, '_blank');
