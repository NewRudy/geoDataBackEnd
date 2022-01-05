package controller;

import dao.CatalogDao;
import dto.CatalogDto;
import entity.base.Catalog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.CatalogService;
import utils.resultUtils.BaseResponse;
import utils.resultUtils.DefaultStatus;

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
            return new BaseResponse(DefaultStatus.SUCCESS, "新建目录成功", catalog.toString());
        } catch (Exception err) {
            logger.warning("/catalog post error: " + err.toString());
            return new BaseResponse(DefaultStatus.FAILURE, "新建目录失败");
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
                return new BaseResponse(DefaultStatus.SUCCESS, "删除目录成功");
            }
            throw new Exception("failed to delete catalog.");
        } catch (Exception err) {
            logger.warning("/catalog delete error: " + err.toString());
            return new BaseResponse(DefaultStatus.FAILURE, "删除目录失败");
        }
    }

    @ApiOperation(value = "根据目录 id 查询目录")
    @RequestMapping(method = RequestMethod.GET)
    public  BaseResponse findById(@RequestParam("id") String catalogId) {
        try{
            Catalog catalog = catalogDao.findOneById(catalogId);
            if(catalog != null) {
                return new BaseResponse(DefaultStatus.SUCCESS, "查询目录成功", catalog.toString());
            } else{
                return new BaseResponse(DefaultStatus.NOT_FOUND, "该目录不存在");
            }
        } catch (Exception err) {
            logger.warning("/catalog get error: " + err.toString());
            return new BaseResponse(DefaultStatus.FAILURE, "查询目录失败");
        }
    }
}
