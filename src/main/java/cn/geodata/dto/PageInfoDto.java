package cn.geodata.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PageInfoDto {
    @ApiModelProperty(value = "当前页数", example = "1")
    private Integer page = 1;
    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "排序顺序", example = "false")
    private Boolean asc = false;
    @ApiModelProperty(value = "排序字段", example = "date")
    private String sortField = "date";
}
