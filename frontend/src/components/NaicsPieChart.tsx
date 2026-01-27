import { Doughnut } from 'react-chartjs-2';
import type { ChartOptions } from 'chart.js';
import type { NaicsShare } from '../types';
import { PALETTE } from '../palette';

interface Props {
  data: NaicsShare[];
}

export default function NaicsPieChart({ data }: Props) {
  const chartData = {
    labels: data.map((d) => d.industry),
    datasets: [
      {
        data: data.map((d) => d.value),
        backgroundColor: PALETTE.slice(0, data.length),
        borderColor: '#151b28',
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };

  const options: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        position: 'right',
        labels: {
          boxWidth: 12,
          padding: 14,
          font: { size: 11 },
          color: '#c1c7d4',
        },
      },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const total = (ctx.dataset.data as number[]).reduce(
              (a, b) => a + b,
              0,
            );
            const pct = ((ctx.parsed / total) * 100).toFixed(1);
            return `${ctx.label}: ${ctx.parsed.toLocaleString()} (${pct}%)`;
          },
        },
      },
    },
  };

  return <Doughnut data={chartData} options={options} />;
}
