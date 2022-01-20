package cn.geodata.controller;

import cn.geodata.dao.SingleFileDao;
import cn.geodata.entity.data.SingleFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.geodata.service.SingleFileService;
import cn.geodata.utils.resultUtils.BaseResponse;
import cn.geodata.enums.ResultStatusEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@Api(value = "文件管理")
@RestController
@RequestMapping("/file")
@Slf4j
/**
 * @description:
 * @author: Tian
 * @date: 2022/1/4 23:10
 */
public class SingleFileController {
    @Autowired
    SingleFileService singleFileService;

    @Autowired
    SingleFileDao singleFileDao;

    Logger logger = Logger.getLogger(SingleFileController.class.getName());

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/5 10:05
     * @param multipartFile
     * @param id
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation("上传文件")
    @RequestMapping(method = RequestMethod.POST)
    public BaseResponse create(@RequestParam("data") MultipartFile multipartFile,
                               @RequestParam("id") String id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "description", required = false) String description
                               ) {
        try {
            SingleFile singleFile = singleFileService.create(multipartFile, id, name, description);
            if(singleFile != null) {
                logger.info("/file post success: " + singleFile.toString());
                return new BaseResponse(ResultStatusEnum.SUCCESS, "上传文件成功", singleFile);
            }
            throw new Exception("singleFile is null.");
        } catch (Exception err) {
            logger.warning("/file post error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "上传文件失败");
        }
    }

    @ApiOperation("下载文件")
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(@RequestParam("id") String id, @RequestParam("catalogId") String catalogId, HttpServletResponse response, @RequestParam(value = "type", required = false) String type) throws Exception {
        singleFileService.download(id, catalogId, response, type);
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/5 10:11
     * @param singleFileId
     * @param catalogId
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation("删除文件")
    @RequestMapping(method = RequestMethod.DELETE)
    public BaseResponse deleteById(@RequestParam("id") String singleFileId, @RequestParam("catalogId") String catalogId) {
        try {
            if(singleFileService.delete(singleFileId, catalogId)) {
                logger.info(String.format("/file delete success: singleFileId: %s, catalogId: %s", singleFileId.toString(), catalogId.toString()) );
                return new BaseResponse(ResultStatusEnum.SUCCESS, "删除文件成功");
            } else {
                logger.warning(String.format("/file delete failed: singleFileId: %s, catalogId: %s, method return false", singleFileId.toString(), catalogId.toString()) );
                return new BaseResponse(ResultStatusEnum.FAILURE, "删除文件失败");
            }
        } catch (Exception err) {
            logger.warning("/file delete error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "删除文件失败");
        }
    }

    /**
     * @description:
     * @author: Tian
     * @date: 2022/1/5 10:12
     * @param singleFileId
     * @return: utils.resultUtils.BaseResponse
     */
    @ApiOperation("查询文件")
    @RequestMapping(method = RequestMethod.GET)
    public BaseResponse findById(@RequestParam("id") String singleFileId) {
        try {
            SingleFile singleFile = singleFileDao.findOneById(singleFileId);
            if(singleFile != null) {
                logger.info("/file get success: " + singleFileId);
                return new BaseResponse(ResultStatusEnum.SUCCESS, "查询文件成功", singleFile);
            } else {
                logger.warning(String.format("/file get failed: %s is not found", singleFileId));
                return new BaseResponse(ResultStatusEnum.NOT_FOUND, "文件id不存在");
            }
        } catch (Exception err) {
            logger.warning("/file get error: " + err.toString());
            return new BaseResponse(ResultStatusEnum.FAILURE, "查询文件失败");
        }
    }
}
