import { Chart } from 'react-chartjs-2';
import type { ChartOptions } from 'chart.js';
import type { CombinedTimeSeries } from '../types';
import { TEAL, BLUE } from '../palette';

interface Props {
  data: CombinedTimeSeries;
}

export default function CombinedChart({ data }: Props) {
  const labels = data.housingStarts.map((p) => p.date);

  const chartData = {
    labels,
    datasets: [
      {
        type: 'bar' as const,
        label: 'Housing Starts',
        data: data.housingStarts.map((p) => p.value),
        backgroundColor: TEAL,
        borderRadius: 2,
        borderSkipped: false,
        yAxisID: 'y',
        order: 2,
      },
      {
        type: 'line' as const,
        label: 'Total Employment',
        data: data.employment.map((p) => p.value),
        borderColor: BLUE,
        backgroundColor: 'transparent',
        borderWidth: 2.5,
        pointRadius: 0,
        pointHoverRadius: 4,
        pointHoverBackgroundColor: BLUE,
        tension: 0.35,
        yAxisID: 'y1',
        order: 1,
      },
    ],
  };

  const options: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: true,
    aspectRatio: 3.8,
    interaction: { mode: 'index', intersect: false },
    plugins: {
      legend: {
        position: 'top',
        align: 'center',
        labels: {
          padding: 20,
          font: { size: 12 },
        },
      },
      tooltip: {
        callbacks: {
          label: (ctx) =>
            `${ctx.dataset.label}: ${(ctx.parsed.y ?? 0).toLocaleString()}`,
        },
      },
    },
    scales: {
      x: {
        ticks: { maxTicksLimit: 14, maxRotation: 45, font: { size: 10 } },
        grid: { display: false },
      },
      y: {
        type: 'linear',
        position: 'left',
        title: {
          display: true,
          text: 'Housing Starts',
          color: '#7a8599',
          font: { size: 11 },
        },
        grid: { color: 'rgba(255,255,255,0.04)' },
        ticks: {
          font: { size: 10 },
          callback: (v) => (typeof v === 'number' ? v.toLocaleString() : v),
        },
      },
      y1: {
        type: 'linear',
        position: 'right',
        title: {
          display: true,
          text: 'Employment (×1 000)',
          color: '#7a8599',
          font: { size: 11 },
        },
        grid: { drawOnChartArea: false },
        ticks: {
          font: { size: 10 },
          callback: (v) =>
            typeof v === 'number' ? `${(v / 1000).toFixed(0)}K` : v,
        },
      },
    },
  };

  return <Chart type="bar" data={chartData as any} options={options} />;
}
