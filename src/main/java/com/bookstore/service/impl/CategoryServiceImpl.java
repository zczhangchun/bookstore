package com.bookstore.service.impl;

import com.bookstore.mapper.CategoryMapper;
import com.bookstore.pojo.Category;
import com.bookstore.service.BookService;
import com.bookstore.service.CategoryService;
import com.bookstore.web.config.PrefixConfig;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@EnableConfigurationProperties(PrefixConfig.class)
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BookService bookService;



    /**
     * 查询分类
     *
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @Override
    public List<Category> queryCategoryByPage(int page, int pageSize, String keyword, Boolean status) {

        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();

        //添加名称模糊条件
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andLike("name", "%" + keyword + "%");
        }

        //添加推荐状态条件
        if (status != null) {
            criteria.andEqualTo("status", status);
        }

        //分页
        PageHelper.startPage(page, pageSize);
        return categoryMapper.selectByExample(example);


    }

    /**
     * 添加分类
     *
     * @param category
     */
    @Override
    @Transactional
    public void addCategory(Category category) {


        categoryMapper.insert(category);

    }

    /**
     * @param data 需要校验的数据
     * @return
     */
    @Override
    public Boolean verifyCategory(String data) {

        Category condition = new Category();
        condition.setName(data);

        return CollectionUtils.isEmpty(categoryMapper.select(condition));
    }

    /**
     * 批量删除分类
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteCategory(List<Integer> ids) {

        if (!CollectionUtils.isEmpty(ids)) {
            categoryMapper.deleteByIdList(ids);
            for (Integer id : ids) {
                //删除分类下的图书
                bookService.deleteBookByCategoryId(id);
            }
        }

    }

    @Override
    @Transactional
    public void updateCategory(Category category) {
        if (category != null) {
            //更新mysql
            categoryMapper.updateByPrimaryKey(category);
        }
    }

    @Override
    public List<Category> queryCategoryByKeyword(String keyword) {

        Example example = new Example(Category.class);
        example.createCriteria().andLike("name", "%" + keyword + "%");
        return categoryMapper.selectByExample(example);

    }

    /**
     * 通过状态查找分类
     *
     * @param status
     * @return
     */
    @Override
    public List<Category> findByStatus(Boolean status) {
        /**
         * 通过状态查询图书分类
         */
        Category condition = new Category();
        condition.setStatus(status);
        List<Category> categoryList = categoryMapper.select(condition);

        return categoryList;
    }

    /**
     * 查找所有分类
     *
     * @return
     */
    @Override
    public List<Category> findAll() {
        /**
         * 查询所有分类
         */
        return categoryMapper.selectAll();
    }

    @Override
    public Category findById(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }
}
