import { useMemo } from 'react';
import type { HeatmapCell } from '../types';

interface Props {
  data: HeatmapCell[];
}

function heatColor(value: number, max: number): { bg: string; fg: string } {
  if (max === 0) return { bg: '#1a2030', fg: '#7a8599' };
  const ratio = Math.min(value / max, 1);

  const r = Math.round(30 + ratio * 190);
  const g = Math.round(40 + ratio * 60);
  const b = Math.round(50 - ratio * 20);
  const bg = `rgb(${r}, ${g}, ${b})`;
  const fg = ratio > 0.35 ? '#fff' : '#c1c7d4';
  return { bg, fg };
}

export default function HeatmapTable({ data }: Props) {
  const { periods, categories, grid, max } = useMemo(() => {
    const periodSet = new Set<string>();
    const categorySet = new Set<string>();
    let maxVal = 0;

    for (const cell of data) {
      periodSet.add(cell.period);
      categorySet.add(cell.category);
      if (cell.value > maxVal) maxVal = cell.value;
    }

    const periods = Array.from(periodSet).sort();
    const categories = Array.from(categorySet).sort();

    const lookup = new Map<string, number>();
    for (const cell of data) {
      lookup.set(`${cell.period}|${cell.category}`, cell.value);
    }

    const grid = periods.map((p) =>
      categories.map((c) => lookup.get(`${p}|${c}`) ?? 0),
    );

    return { periods, categories, grid, max: maxVal };
  }, [data]);

  if (data.length === 0) {
    return <p className="empty-message">No heatmap data available.</p>;
  }

  return (
    <div className="heatmap-wrap">
      <table className="heatmap-table">
        <thead>
          <tr>
            <th></th>
            {periods.map((p) => (
              <th key={p}>{p}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {categories.map((c, ci) => (
            <tr key={c}>
              <td>{c}</td>
              {grid.map((row, pi) => {
                const val = row[ci];
                const { bg, fg } = heatColor(val, max);
                return (
                  <td
                    key={periods[pi]}
                    style={{ backgroundColor: bg, color: fg }}
                  >
                    {Math.round(val).toLocaleString()}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
