package com.xdx.community.service;

import com.xdx.community.dto.PaginationDTO;
import com.xdx.community.dto.QuestionDto;
import com.xdx.community.dto.QuestionQueryDTO;
import com.xdx.community.exception.CustomizeErrorCode;
import com.xdx.community.exception.CustomizeException;
import com.xdx.community.mapper.QuestionExtMapper;
import com.xdx.community.mapper.QuestionMapper;
import com.xdx.community.mapper.UserMapper;
import com.xdx.community.model.Question;
import com.xdx.community.model.QuestionExample;
import com.xdx.community.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QuestionExtMapper questionExtMapper;

    /**
     * 返回dtoList
     * @param page ：当前页
     * @param pageSize ：每页显示数目
     * @return
     */
    public PaginationDTO questionList(String search,Integer page, Integer pageSize){
        PaginationDTO<QuestionDto> paginationDTO =new PaginationDTO<QuestionDto>();

        //判断serach是否为空，不为空进行切割空格.然后按照|拼接成一个字符串
        if(StringUtils.isNotBlank(search)){
            String serachs[] = search.split(" ");
            search = Arrays.stream(serachs).collect(Collectors.joining("|"));
        }
        //根据serach查找问题总数
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        Integer totalCount = questionExtMapper.countBySearch(questionQueryDTO);
        Integer totalPage = totalCount/pageSize;

        Integer startCount = page<1? 1 : pageSize * (page-1);
        if (totalCount % pageSize == 0) {
            totalPage = totalCount / pageSize;
        } else {
            totalPage = totalCount / pageSize + 1;
        }
        paginationDTO.setTotalPage(totalPage);
        List<QuestionDto> dtoList =new ArrayList<QuestionDto>();

        questionQueryDTO.setPage(startCount);
        questionQueryDTO.setSize(pageSize);
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        for (Question question:questions) {
            Long creator_id = question.getCreator();
            User user = userMapper.selectByPrimaryKey(creator_id);

            QuestionDto dto = new QuestionDto();
            BeanUtils.copyProperties(question,dto);
            dto.setUser(user);

            dtoList.add(dto);
        }
        paginationDTO.setData(dtoList);

        paginationDTO.setPagination(totalPage,page);

        return paginationDTO;
    }

    public PaginationDTO getQuestByCreatorID(Long id, Integer page, Integer pageSize) {
        PaginationDTO<QuestionDto> paginationDTO =new PaginationDTO<QuestionDto>();

        Integer startCount = page<1? 1 : pageSize * (page-1);
        //根据用户id查找发布的问题总数
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria()
                .andCreatorEqualTo(id);
        Integer totalCount = (int)questionMapper.countByExample(questionExample);
        Integer totalPage = totalCount/pageSize;

        if (totalCount % pageSize == 0) {
            totalPage = totalCount / pageSize;
        } else {
            totalPage = totalCount / pageSize + 1;
        }

        paginationDTO.setTotalPage(totalPage);
        List<QuestionDto> dtoList =new ArrayList<QuestionDto>();
        QuestionExample questionExample1 = new QuestionExample();
        questionExample.createCriteria().andIdEqualTo(id);
        List<Question> myQuestion = questionMapper.selectByExampleWithRowbounds(questionExample,new RowBounds(startCount,pageSize));

        for (Question question:myQuestion) {
            Long creator_id = question.getCreator();
            User user = userMapper.selectByPrimaryKey(creator_id);
            QuestionDto dto = new QuestionDto();
            BeanUtils.copyProperties(question,dto);
            dto.setUser(user);
            dtoList.add(dto);
        }
        paginationDTO.setData(dtoList);
        paginationDTO.setPagination(totalPage,page);
        return paginationDTO;
    }

    /**
     *
     * @param id
     * @return
     */
    public QuestionDto getQuestionbyId(Long id) {
        QuestionDto questionDto =new QuestionDto();
        Question question =  questionMapper.selectByPrimaryKey(id);
        if(question==null){
            throw  new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        else{
            BeanUtils.copyProperties(question,questionDto);
            Long userId = question.getCreator();
            User user = userMapper.selectByPrimaryKey(userId);
            questionDto.setUser(user);
        }
        return questionDto;
    }

    public void createOrUpdate(Question question) {

        if(question.getId()==null || question.getId()!=0){
            //如果id为空，那么就是发布一个问题
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            questionMapper.insert(question);
        }
        else{
            question.setGmtModified(question.getGmtCreate());
            int i = questionMapper.updateByPrimaryKey(question);
            if(i==0){
                throw  new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }


    }

    /**
     * 根据id查询问题
     * @param id
     * @return
     */
    public Question getById(Long id) {
        Question questionById = questionMapper.selectByPrimaryKey(id);
        return questionById;
    }

    /**
     * 增加阅读量【浏览数】
     * @param id
     */
    public void addViewCount(Long id) {

        /**
         * 高并发下并不安全
         *         Question question = questionMapper.selectByPrimaryKey(id);
         *         Question updateQuestion = new Question();
         *         updateQuestion.setViewCount(question.getViewCount()+1);
         *         QuestionExample questionExample = new QuestionExample();
         *         questionExample.createCriteria().andIdEqualTo(id);
         *  使用自己定义的Mapper吧
         */
        Question updateQuestion = new Question();
        updateQuestion.setId(id);
        updateQuestion.setViewCount(1L);//每次浏览加上1
        questionExtMapper.addViewCount(updateQuestion);

    }

    /**
     * 查找相关问题
     * @param questionDto
     * @return
     */
    public List<QuestionDto> selectRelated(QuestionDto questionDto) {
        if (StringUtils.isBlank(questionDto.getTags())) {
            return new ArrayList<>();
        }
        //切割标签
        String[] tags = StringUtils.split(questionDto.getTags(), ",");
        //将切割后的标签数组，里面的+，*特殊字符变成空字符串，并且数组元素中间以| 连接起来
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(questionDto.getId());
        question.setTags(regexpTag);

        List<Question> questions = questionExtMapper.selectRelated(question);
        List<QuestionDto> questionDTOS = questions.stream().map(q -> {
            QuestionDto questionDTO = new QuestionDto();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }
}
