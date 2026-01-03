import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
);

ChartJS.defaults.color = '#8b95a8';
ChartJS.defaults.borderColor = 'rgba(255,255,255,0.06)';
ChartJS.defaults.font.family = "'DM Sans', sans-serif";

ChartJS.defaults.plugins.tooltip.backgroundColor = '#1a2030';
ChartJS.defaults.plugins.tooltip.titleColor = '#e4e7ec';
ChartJS.defaults.plugins.tooltip.bodyColor = '#c1c7d4';
ChartJS.defaults.plugins.tooltip.borderColor = '#2a3344';
ChartJS.defaults.plugins.tooltip.borderWidth = 1;
ChartJS.defaults.plugins.tooltip.cornerRadius = 8;
ChartJS.defaults.plugins.tooltip.padding = 12;

ChartJS.defaults.plugins.legend.labels.usePointStyle = true;
ChartJS.defaults.plugins.legend.labels.pointStyleWidth = 8;
