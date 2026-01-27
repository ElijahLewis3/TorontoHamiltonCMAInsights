import { Bar } from 'react-chartjs-2';
import type { ChartOptions } from 'chart.js';
import type { HousingCategorySummary } from '../types';
import { TEAL, GOLD } from '../palette';

interface Props {
  data: HousingCategorySummary[];
}

export default function HousingCategoryChart({ data }: Props) {
  const chartData = {
    labels: data.map((d) => d.category),
    datasets: [
      {
        label: 'Starts',
        data: data.map((d) => d.starts),
        backgroundColor: TEAL,
        borderRadius: 4,
        borderSkipped: false,
      },
      {
        label: 'Completions',
        data: data.map((d) => d.completions),
        backgroundColor: GOLD,
        borderRadius: 4,
        borderSkipped: false,
      },
    ],
  };

  const options: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        position: 'top',
        labels: { padding: 16, font: { size: 11 } },
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
        grid: { display: false },
        ticks: { font: { size: 11 } },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(255,255,255,0.04)' },
        ticks: {
          font: { size: 10 },
          callback: (v) =>
            typeof v === 'number' ? v.toLocaleString() : v,
        },
      },
    },
  };

  return <Bar data={chartData} options={options} />;
}
