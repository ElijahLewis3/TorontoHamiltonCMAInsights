import { useCallback, useEffect, useState } from 'react';
import type {
  Region,
  CombinedTimeSeries,
  NaicsShare,
  HousingCategorySummary,
  HeatmapCell,
} from './types';
import {
  fetchRegions,
  fetchCombined,
  fetchNaics,
  fetchHousingCategories,
  fetchHeatmap,
  refreshData,
  exportEmployment,
  exportHousing,
} from './api';
import CombinedChart from './components/CombinedChart';
import NaicsPieChart from './components/NaicsPieChart';
import HousingCategoryChart from './components/HousingCategoryChart';
import HeatmapTable from './components/HeatmapTable';

type DataView = 'Housing' | 'Employment';

export default function App() {
  const [regions, setRegions] = useState<Region[]>([]);
  const [selectedCode, setSelectedCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dataView, setDataView] = useState<DataView>('Housing');
  const [refreshing, setRefreshing] = useState(false);

  const [combined, setCombined] = useState<CombinedTimeSeries | null>(null);
  const [naics, setNaics] = useState<NaicsShare[]>([]);
  const [housingCategories, setHousingCategories] = useState<HousingCategorySummary[]>([]);
  const [heatmap, setHeatmap] = useState<HeatmapCell[]>([]);

  const selectedRegion = regions.find((r) => r.code === selectedCode);

  const loadInsights = useCallback(async (code: string) => {
    setLoading(true);
    setError(null);
    try {
      const [c, n, h, hm] = await Promise.all([
        fetchCombined(code),
        fetchNaics(code),
        fetchHousingCategories(code),
        fetchHeatmap(code),
      ]);
      setCombined(c);
      setNaics(n);
      setHousingCategories(h);
      setHeatmap(hm);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const r = await fetchRegions();
        setRegions(r);
        if (r.length > 0) {
          setSelectedCode(r[0].code);
          await loadInsights(r[0].code);
        } else {
          setLoading(false);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load regions');
        setLoading(false);
      }
    })();
  }, [loadInsights]);

  const handleRegionChange = (code: string) => {
    setSelectedCode(code);
    loadInsights(code);
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    setError(null);
    try {
      await refreshData();
      if (selectedCode) await loadInsights(selectedCode);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Refresh failed');
    } finally {
      setRefreshing(false);
    }
  };

  const regionLabel = selectedRegion
    ? `${selectedRegion.name} CMA`
    : '';

  return (
    <>
      <header className="app-header">
        <h1>
          Multi-Family Housing <em>&amp; Employment</em> Insights
        </h1>
        <p className="subtitle">
          Explore how multi-family housing activity relates to regional employment
          across Canadian census metropolitan areas.
        </p>
      </header>

      <div className="controls-bar">
        <div className="control-group">
          <span className="control-label">Region</span>
          <select
            className="region-selector"
            value={selectedCode}
            onChange={(e) => handleRegionChange(e.target.value)}
            disabled={loading}
          >
            {regions.map((r) => (
              <option key={r.id} value={r.code}>
                {r.name} CMA
              </option>
            ))}
          </select>
        </div>

        <div className="control-group">
          <span className="control-label">Data Type</span>
          <select
            className="region-selector"
            value={dataView}
            onChange={(e) => setDataView(e.target.value as DataView)}
          >
            <option value="Housing">Housing</option>
            <option value="Employment">Employment</option>
          </select>
        </div>

        <button
          className="btn btn-export"
          onClick={() =>
            dataView === 'Housing'
              ? exportHousing(selectedCode)
              : exportEmployment(selectedCode)
          }
          disabled={!selectedCode}
        >
          Export {dataView} CSV <span className="arrow">↓</span>
        </button>

        <button
          className="btn btn-refresh"
          onClick={handleRefresh}
          disabled={refreshing || loading}
        >
          {refreshing ? 'Refreshing…' : '↻ Refresh Data'}
        </button>
      </div>

      <main className="dashboard">
        {loading && (
          <div className="loading">
            <div className="spinner" />
            <span className="loading-text">Loading data…</span>
          </div>
        )}

        {error && <div className="error">{error}</div>}

        {!loading && !error && (
          <>
            <div className="card card--wide">
              <div className="card-header">
                <h2 className="card-title">Housing Starts vs Employment</h2>
                {regionLabel && <span className="badge">{regionLabel}</span>}
              </div>
              {combined && <CombinedChart data={combined} />}
            </div>

            {dataView === 'Housing' ? (
              <>
                <div className="card">
                  <div className="card-header">
                    <h2 className="card-title">Starts vs Completions by Category</h2>
                    {regionLabel && <span className="badge">{regionLabel}</span>}
                  </div>
                  <HousingCategoryChart data={housingCategories} />
                </div>

                <div className="card">
                  <div className="card-header">
                    <h2 className="card-title">Housing Starts Heat Map</h2>
                    {regionLabel && <span className="badge">{regionLabel}</span>}
                  </div>
                  <HeatmapTable data={heatmap} />
                </div>
              </>
            ) : (
              <div className="card card--wide">
                <div className="card-header">
                  <h2 className="card-title">Employment by Industry (NAICS)</h2>
                  {regionLabel && <span className="badge">{regionLabel}</span>}
                </div>
                <NaicsPieChart data={naics} />
              </div>
            )}
          </>
        )}
      </main>
    </>
  );
}
