package me.cho.snackball.study.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchConditions {
    private String keyword;
    private String tag;
    private String location;
}
