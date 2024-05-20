package com.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CricketMatchDataVisualization extends JFrame {

    private List<MatchData> matchDataList;

    public CricketMatchDataVisualization(String title) {
        super(title);
        this.matchDataList = loadCSVData("match.csv");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        CricketMatchDataVisualization app = new CricketMatchDataVisualization("Cricket Match Data Visualization");
        app.menu();
    }

    private void menu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nCricket Match Data Visualization Menu");
            System.out.println("1. Distribution of Wins by Runs vs Wins by Wickets");
            System.out.println("2. Comparison of Runs Scored in Innings 1 vs Innings 2");
            System.out.println("3. Distribution of Winning Margins by Runs and Wickets");
            System.out.println("4. Number of Matches Won by Home Team at Each Venue");
            System.out.println("5. Total Runs Scored in Each Match Over Time");
            System.out.println("6. Balls Remaining in Successful Run Chases");
            System.out.println("7. Exit");
            System.out.print("Enter your choice (1-7): ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    plotWinDistribution();
                    break;
                case 2:
                    plotRunsComparison();
                    break;
                case 3:
                    plotWinningMargins();
                    break;
                case 4:
                    plotVenueWins();
                    break;
                case 5:
                    plotTotalRunsOverTime();
                    break;
                case 6:
                    plotBallsRemainingInChases();
                    break;
                case 7:
                    System.out.println("Exiting the program.");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }

    private void plotWinDistribution() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        long winByRuns = matchDataList.stream().filter(match -> match.getWinByRuns() != null).count();
        long winByWickets = matchDataList.stream().filter(match -> match.getWinByWickets() != null).count();

        dataset.setValue("Wins by Runs", winByRuns);
        dataset.setValue("Wins by Wickets", winByWickets);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Distribution of Wins by Runs vs Wins by Wickets",
                dataset,
                true, true, false);

        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSectionPaint("Wins by Runs", new Color(255, 153, 153));
        plot.setSectionPaint("Wins by Wickets", new Color(102, 179, 255));

        displayChart(pieChart);
    }

    private void plotRunsComparison() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        List<Double> innings1Runs = new ArrayList<>();
        List<Double> innings2Runs = new ArrayList<>();
        for (MatchData match : matchDataList) {
            innings1Runs.add((double) match.getInnings1Runs());
            innings2Runs.add((double) match.getInnings2Runs());
        }

        dataset.add(innings1Runs, "Innings 1", "Runs");
        dataset.add(innings2Runs, "Innings 2", "Runs");

        JFreeChart boxPlot = ChartFactory.createBoxAndWhiskerChart(
                "Comparison of Runs Scored in Innings 1 vs Innings 2",
                "Innings",
                "Runs",
                dataset,
                true);

        displayChart(boxPlot);
    }

    private void plotWinningMargins() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        matchDataList.stream()
                .filter(match -> match.getWinByRuns() != null)
                .forEach(match -> dataset.addValue(match.getWinByRuns(), "Win by Runs", match.getDate().toString()));

        matchDataList.stream()
                .filter(match -> match.getWinByWickets() != null)
                .forEach(match -> dataset.addValue(match.getWinByWickets(), "Win by Wickets", match.getDate().toString()));

        JFreeChart barChart = ChartFactory.createBarChart(
                "Distribution of Winning Margins by Runs and Wickets",
                "Date",
                "Margin",
                dataset);

        displayChart(barChart);
    }

    private void plotVenueWins() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        matchDataList.stream()
                .filter(match -> match.getWinner() != null && match.getWinner().equals(match.getHome()))
                .forEach(match -> dataset.addValue(1, match.getVenue(), match.getDate().toString()));

        JFreeChart barChart = ChartFactory.createBarChart(
                "Number of Matches Won by Home Team at Each Venue",
                "Date",
                "Number of Wins",
                dataset);

        displayChart(barChart);
    }

    private void plotTotalRunsOverTime() {
        XYSeries series = new XYSeries("Total Runs");

        matchDataList.forEach(match -> {
            int totalRuns = match.getInnings1Runs() + match.getInnings2Runs();
            series.add(match.getDate().toEpochDay(), totalRuns);
        });

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart xyChart = ChartFactory.createXYLineChart(
                "Total Runs Scored in Each Match Over Time",
                "Date",
                "Total Runs",
                dataset);

        displayChart(xyChart);
    }

    private void plotBallsRemainingInChases() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        matchDataList.stream()
                .filter(match -> match.getBallsRemaining() != null)
                .forEach(match -> dataset.addValue(match.getBallsRemaining(), "Balls Remaining", match.getDate().toString()));

        JFreeChart barChart = ChartFactory.createBarChart(
                "Balls Remaining in Successful Run Chases",
                "Date",
                "Balls Remaining",
                dataset);

        displayChart(barChart);
    }

    private void displayChart(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
        pack();
        setVisible(true);
    }

    public static List<MatchData> loadCSVData(String filePath) {
        List<MatchData> matchDataList = new ArrayList<>();
        try (FileReader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord record : csvParser) {
                MatchData match = new MatchData(
                        Integer.parseInt(record.get("match_id")),
                        record.get("series_id"),
                        record.get("match_details"),
                        record.get("result"),
                        record.get("scores"),
                        LocalDate.parse(record.get("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        record.get("venue"),
                        record.get("round"),
                        record.get("home"),
                        record.get("away"),
                        record.get("winner"),
                        record.isSet("win_by_runs") ? Integer.parseInt(record.get("win_by_runs")) : null,
                        record.isSet("win_by_wickets") ? Integer.parseInt(record.get("win_by_wickets")) : null,
                        record.isSet("balls_remaining") ? Integer.parseInt(record.get("balls_remaining")) : null,
                        record.isSet("innings1_runs") ? Integer.parseInt(record.get("innings1_runs")) : 0,
                        record.isSet("innings2_runs") ? Integer.parseInt(record.get("innings2_runs")) : 0
                );
                matchDataList.add(match);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matchDataList;
    }
    

    class MatchData {
        private int matchId;
        private String seriesId;
        private String matchDetails;
        private String result;
        private String scores;
        private LocalDate date;
        private String venue;
        private String round;
        private String home;
        private String away;
        private String winner;
        private Integer winByRuns;
        private Integer winByWickets;
        private Integer ballsRemaining;
        private Integer innings1Runs;
        private Integer innings2Runs;

        public MatchData(int matchId, String seriesId, String matchDetails, String result, String scores, LocalDate date, String venue, String round, String home, String away, String winner, Integer winByRuns, Integer winByWickets, Integer ballsRemaining, Integer innings1Runs, Integer innings2Runs) {
            this.matchId = matchId;
            this.seriesId = seriesId;
            this.matchDetails = matchDetails;
            this.result = result;
            this.scores = scores;
            this.date = date;
            this.venue = venue;
            this.round = round;
            this.home = home;
            this.away = away;
            this.winner = winner;
            this.winByRuns = winByRuns;
            this.winByWickets = winByWickets;
            this.ballsRemaining = ballsRemaining;
            this.innings1Runs = innings1Runs;
            this.innings2Runs = innings2Runs;
        }

        public int getMatchId() {
            return matchId;
        }

        public String getSeriesId() {
            return seriesId;
        }

        public String getMatchDetails() {
            return matchDetails;
        }

        public String getResult() {
            return result;
        }

        public String getScores() {
            return scores;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getVenue() {
            return venue;
        }

        public String getRound() {
            return round;
        }

        public String getHome() {
            return home;
        }

        public String getAway() {
            return away;
        }

        public String getWinner() {
            return winner;
        }

        public Integer getWinByRuns() {
            return winByRuns;
        }

        public Integer getWinByWickets() {
            return winByWickets;
        }

        public Integer getBallsRemaining() {
            return ballsRemaining;
        }

        public Integer getInnings1Runs() {
            return innings1Runs;
        }

        public Integer getInnings2Runs() {
            return innings2Runs;
        }
    }
}
