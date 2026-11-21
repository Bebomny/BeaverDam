package dev.bebomny.beaverdam.watchpost.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShowSeriesTest {

    private ShowSeries newSeries(boolean interesting, boolean ignored, boolean autoDownload) {
        return ShowSeries.builder()
                .id(1L)
                .seriesName("Test Anime")
                .isInteresting(interesting)
                .isIgnored(ignored)
                .autoDownload(autoDownload)
                .submitted(false)
                .customShareRatio("2.0")
                .build();
    }

    @Test
    void markInterestingTrueClearsIgnored() {
        ShowSeries series = newSeries(false, true, false);

        series.markInteresting(true);

        assertThat(series.getIsInteresting()).isTrue();
        assertThat(series.getIsIgnored()).isFalse();
        assertThat(series.getAutoDownload()).isFalse();
    }

    @Test
    void markInterestingFalseClearsAutoDownload() {
        ShowSeries series = newSeries(true, false, true);

        series.markInteresting(false);

        assertThat(series.getIsInteresting()).isFalse();
        assertThat(series.getAutoDownload()).isFalse();
    }

    @Test
    void markIgnoredTrueClearsInterestingAndAutoDownload() {
        ShowSeries series = newSeries(true, false, true);

        series.markIgnored(true);

        assertThat(series.getIsIgnored()).isTrue();
        assertThat(series.getIsInteresting()).isFalse();
        assertThat(series.getAutoDownload()).isFalse();
    }

    @Test
    void setAutoDownloadEnabledTrueForcesInterestingAndClearsIgnored() {
        ShowSeries series = newSeries(false, true, false);

        series.setAutoDownloadEnabled(true);

        assertThat(series.getAutoDownload()).isTrue();
        assertThat(series.getIsInteresting()).isTrue();
        assertThat(series.getIsIgnored()).isFalse();
    }

    @Test
    void applyStateUpdateValidFullCombinationsSucceedAndMarkSubmitted() {
        ShowSeries series = newSeries(false, false, false);

        series.applyStateUpdate(true, false, true, "3.0");
        assertThat(series.getIsInteresting()).isTrue();
        assertThat(series.getIsIgnored()).isFalse();
        assertThat(series.getAutoDownload()).isTrue();
        assertThat(series.getCustomShareRatio()).isEqualTo("3.0");
        assertThat(series.getSubmitted()).isTrue();

        series.applyStateUpdate(false, true, false, null);
        assertThat(series.getIsInteresting()).isFalse();
        assertThat(series.getIsIgnored()).isTrue();
        assertThat(series.getAutoDownload()).isFalse();
        assertThat(series.getCustomShareRatio()).isEqualTo("3.0");
    }

    @Test
    void applyStateUpdatePartialAutoDownloadAutoTransitionsState() {
        ShowSeries series = newSeries(false, true, false);

        series.applyStateUpdate(null, null, true, null);

        assertThat(series.getAutoDownload()).isTrue();
        assertThat(series.getIsInteresting()).isTrue();
        assertThat(series.getIsIgnored()).isFalse();
        assertThat(series.getSubmitted()).isTrue();
    }

    @Test
    void applyStateUpdateRejectsContradictoryCombinations() {
        ShowSeries series = newSeries(false, false, false);

        assertThatThrownBy(() -> series.applyStateUpdate(true, true, false, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ignored and interesting");

        assertThatThrownBy(() -> series.applyStateUpdate(false, true, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("autoDownload");

        assertThatThrownBy(() -> series.applyStateUpdate(false, false, true, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("interesting");

        assertThat(series.getSubmitted()).isFalse();
    }
}
