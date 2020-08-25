package com.warest.mall.controller.backend;

import com.google.common.collect.Maps;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Product;
import com.warest.mall.domain.User;
import com.warest.mall.service.IFileService;
import com.warest.mall.service.IProductService;
import com.warest.mall.service.IUserService;
import com.warest.mall.util.FTPUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by geely
 */

@RestController
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    //todo  登录拦截器
    private ResponseEntity<String> checkAdminLogin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "管理员未登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return null;
        } else {
            return ResponseEntity.createByErrorMessage("当前用户无权限，请联系管理员操作");
        }
    }

    /**
     * 新增/更新产品
     *
     * @param session
     * @param product
     * @return
     */
    @PostMapping("save")
    // @ResponseBody
    public ResponseEntity productSave(HttpSession session, Product product) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //填充我们增加产品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return responseEntity;
        }
    }

    /**
     * 产品上下架
     *
     * @param session
     * @param productId
     * @param status 1在售2下架3删除
     * @return
     */
    @PostMapping("set_sale_status")
    // @ResponseBody
    public ResponseEntity setSaleStatus(HttpSession session, Integer productId, Integer status) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            return iProductService.setSaleStatus(productId, status);
        } else {
            return responseEntity;
        }

    }

    /**
     * 产品详情
     *
     * @param session
     * @param productId
     * @return
     */
    @PostMapping("detail")
    // @ResponseBody
    public ResponseEntity getDetail(HttpSession session, Integer productId) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //填充业务
            return iProductService.manageProductDetail(productId);
        } else {
            return responseEntity;
        }

    }

    /**
     * 商品分页查询结果
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("list")
    // @ResponseBody
    public ResponseEntity getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //填充业务
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return responseEntity;
        }

    }

    /**
     * 搜索产品  根据商品名
     *
     * @param session
     * @param productName 产品名
     * @param productId   产品id
     * @param pageNum     分页信息
     * @param pageSize
     * @return
     */
    @GetMapping("search")
    // @ResponseBody
    public ResponseEntity productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //填充业务
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return responseEntity;
        }
    }

    /**
     * 文件上传
     *
     * @param session
     * @param file    MultipartFile类型  required为false表示非必须传的参数
     * @param request 调用请求对象
     * @return
     */
    @PostMapping("upload")
    // @ResponseBody
    public ResponseEntity upload(HttpSession session, @RequestParam(value = "upload_file", required = true) MultipartFile file, HttpServletRequest request) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            String path = request.getSession().getServletContext().getRealPath("upload"); //web服务器绝对路径下的upload文件夹  位于WEB_INF下创建一个名为upload的文件夹
            String targetFileName = iFileService.upload(file, path);
            Map fileMap = Maps.newHashMap();
            if (targetFileName == null) {
                return ResponseEntity.createByErrorMessage("上传文件失败");
            }
            String url = FTPUtil.FTP_PREFIX + targetFileName;
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ResponseEntity.createBySuccess(fileMap);
        } else {
            return responseEntity;
        }
    }

    @PostMapping("test_upload")
    // @ResponseBody
    public ResponseEntity testUpload(HttpSession session, @RequestParam(value = "upload_file", required = true) MultipartFile file, HttpServletRequest request) {

        String path = request.getSession().getServletContext().getRealPath("upload"); //web服务器绝对路径下的upload文件夹  位于WEB_INF下创建一个名为upload的文件夹
        String targetFileName = iFileService.upload(file, path);
        Map fileMap = Maps.newHashMap();
        if (targetFileName == null) {
            return ResponseEntity.createByErrorMessage("上传文件失败");
        }
        String url = FTPUtil.FTP_PREFIX + targetFileName;
        fileMap.put("uri", targetFileName);
        fileMap.put("url", url);
        return ResponseEntity.createBySuccess(fileMap);

    }


    /**
     * 富文本中图片上传
     *
     * @param session
     * @param file
     * @param request
     * @param response 修改响应头用
     * @return
     */
    @PostMapping("richtext_img_upload")
    // @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = true) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "管理员未登录");
            return resultMap;
        }
        //富文本中对于返回值json格式有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
        //        {
        //            "success": true/false,
        //                "msg": "error message", # optional
        //            "file_path": "[real file path]"
        //        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }
            String url = FTPUtil.FTP_PREFIX + targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            //修改header  根据文档
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "当前用户无权限，请联系管理员操作");
            return resultMap;
        }
    }


    @PostMapping("test_richtext_img_upload")
    // @ResponseBody
    public Map testRichtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = true) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(file, path);
        if (StringUtils.isBlank(targetFileName)) {
            resultMap.put("success", false);
            resultMap.put("msg", "上传失败");
            return resultMap;
        }
        String url = FTPUtil.FTP_PREFIX + targetFileName;
        resultMap.put("success", true);
        resultMap.put("msg", "上传成功");
        resultMap.put("file_path", url);
        //修改header  根据文档
        response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
        return resultMap;

    }


}
