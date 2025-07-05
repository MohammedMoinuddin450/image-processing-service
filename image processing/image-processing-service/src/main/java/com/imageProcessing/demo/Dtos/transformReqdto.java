package com.imageProcessing.demo.Dtos;

import lombok.Data;

@Data
public class transformReqdto {

    private Resize resize;
    private Integer rotate;
    private String format;
    private Filters filters;
    private Crop crop;
    private Boolean flip;
    private Boolean mirror;
    private Boolean compress;

    @Data
    public static class Resize {
        private Integer width;
        private Integer height;
    }

    @Data
    public static class Filters {
        private Boolean grayscale;
        private Boolean sepia;
    }

    @Data
    public static class Crop {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
