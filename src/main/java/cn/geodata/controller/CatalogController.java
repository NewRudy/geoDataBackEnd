package cn.geodata.controller;

import cn.geodata.dao.CatalogDao;
import cn.geodata.dto.CatalogDto;
import cn.geodata.dto.PageInfoDto;
import cn.geodata.entity.base.Catalog;
import cn.geodata.entity.base.ChildrenData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.geodata.service.CatalogService;
import cn.geodata.utils.resultUtils.BaseResponse;
import cn.geodata.enums.ResultStatusEnum;

import java.util.logging.Logger;

@Api(tags = "目录管理")
@RestController
@RequestMapping(value = "/catalog")
@Slf4j
/**
 * @description:
 * @author: Tian
 * @date: 2022/1/4 22:33
 */
public class CatalogController {
    @Autowired
    CatalogDao catalogDao;

    @Autowired
    CatalogService catalogService;

    Logger logger = Logger.getLogger(CatalogController.class.getName());

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/4 22:48
     * @param catalogDto
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation(value = "新建目录")
    @RequestMapping(method = RequestMethod.POST)
    public BaseResponse create(@RequestBody CatalogDto catalogDto) {
        try{
            Catalog catalog = catalogService.create(catalogDto.getUserId(), catalogDto.getParentId(), catalogDto.getName());
            return new BaseResponse(ResultStatusEnum.SUCCESS, "新建目录成功", catalog);
        } catch (Exception err) {
            logger.warning("/catalog post error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "新建目录失败");
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/4 22:52
     * @param catalogId
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation(value = "根据目录 id 删除目录")
    @RequestMapping(method = RequestMethod.DELETE)
    public BaseResponse deleteById(@RequestParam("id") String catalogId) {
        try{
            if(catalogService.delete(catalogId)) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "删除目录成功");
            }
            throw new Exception("failed to delete catalog.");
        } catch (Exception err) {
            logger.warning("/catalog delete error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "删除目录失败");
        }
    }

    // @ApiOperation(value = "修改目录的值")
    // @RequestMapping(method = "/update", method = RequestMethod.POST)
    // public  BaseResponse update(@RequestParam("catalogId") String catalogId, @RequestParam("id") String id, @RequestParam("name") String name, @RequestParam)

    @ApiOperation(value = "修改目录下 children 里面的名字和描述")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public BaseResponse updateChildrenData(@RequestParam("catalogId") String catalogId, @RequestParam("id") String id, @RequestParam("type") String type, @RequestParam("content") String content) {
        try{
            if(catalogService.updateChildrenData(catalogId, id, type, content)) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "更改目录成功");
            } else {
                return new BaseResponse(ResultStatusEnum.FAILURE, "更改目录失败");
            }
        } catch (Exception err) {
            logger.warning("/update for catalog is wrong: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "更改目录失败: " + err.toString());
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/3/4 11:00
     * @param catalogId
     * @return: cn.geodata.utils.resultUtils.BaseResponse
     */
    @ApiOperation(value = "根据目录 id 查询目录")
    @RequestMapping(method = RequestMethod.GET)
    public  BaseResponse findById(@RequestParam("id") String catalogId) {
        try{
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog != null) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "查询目录成功", catalog);
            } else{
                return new BaseResponse(ResultStatusEnum.NOT_FOUND, "该目录不存在");
            }
        } catch (Exception err) {
            logger.warning("/catalog get by id error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "查询目录失败");
        }
    }

    @ApiOperation(value = "根据 id 查询目录，并且对 children 进行分页")
    @RequestMapping(value = "/findByIdAndPage", method = RequestMethod.POST)
    public BaseResponse findByIdAndPage(@RequestParam("id") String catalogId, @RequestBody PageInfoDto pageInfoDto) {
        try {
            Catalog catalog = catalogService.findByMultiItem(catalogId, pageInfoDto);
            if(catalog != null) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "查询目录成功", catalog);
            } else{
                return new BaseResponse(ResultStatusEnum.NOT_FOUND, "该目录不存在");
            }
        } catch (Exception err) {
            logger.warning("/catalog get by id and page error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "查询目录失败");
        }
    }

    @ApiOperation(value = "根据 searchContent 查询目录，并且对 children 进行分页")
    @RequestMapping(value = "findByItems", method = RequestMethod.POST)
    public BaseResponse findByItems(@RequestParam("id") String catalogId, @RequestParam("item") String searchItem,
                                    @RequestParam("content") String searchContent, @RequestBody PageInfoDto pageInfoDto) {
        try {
            Catalog catalog = catalogService.findByMultiItem(catalogId, pageInfoDto, searchItem, searchContent);
            if(catalog != null) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "查询目录成功", catalog);
            } else{
                return new BaseResponse(ResultStatusEnum.NOT_FOUND, "该目录不存在");
            }
        } catch (Exception err) {
            logger.warning("/catalog get by id and page error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "查询目录失败: " + err.toString());
        }
    }

    @ApiOperation(value = "查询 childrenData")
    @RequestMapping(value = "findChildrenData", method = RequestMethod.GET)
    public BaseResponse findChildrenData(@RequestParam("catalogId") String catalogId, @RequestParam("id") String id) {
        try {
            ChildrenData childrenData = catalogService.findChildrenData(catalogId, id);
            if(childrenData != null) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "查询 childrenData 成功", childrenData);
            } else {
                return new BaseResponse(ResultStatusEnum.FAILURE, "查询 childrenData 失败" );
            }
        } catch (Exception err) {
            logger.warning("/findChildrenData error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "查询 childrenData 失败: " + err.toString());
        }
    }

    @ApiOperation(value = "复制文件或文件夹")
    @RequestMapping(value = "copy", method = RequestMethod.POST)
    public BaseResponse copy(@RequestParam("catalogId") String catalogId, @RequestBody ChildrenData childrenData) {
        try {
            Boolean flag = Boolean.FALSE;
            if(childrenData.getType().equals("file")) {
                flag = catalogService.copyFile(catalogId, childrenData);
            } else if (childrenData.getType().equals("folder")) {
                flag = catalogService.copyFolder(catalogId, childrenData);
            }
            if(flag) {
                return new BaseResponse(ResultStatusEnum.SUCCESS, "复制目录成功");
            } else {
                return new BaseResponse(ResultStatusEnum.FAILURE, "复制目录失败");
            }
        } catch (Exception err) {
            logger.warning("/copy error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "复制目录失败: " + err.toString());
        }
    }

}
