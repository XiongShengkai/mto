package me.tianle.login.controller;

import me.tianle.login.bean.DataSourceRequest;
import me.tianle.login.unit.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Mto {

  private static final Logger logger = LoggerFactory.getLogger(Mto.class);

  @RequestMapping("/")
  public String index(Model model) {
	//Person single = new Person("aa", 11);
	//List<Person> people = new ArrayList<>();
	//Person p1 = new Person("zhangsan", 11);
	//Person p2 = new Person("lisi", 22);
	//Person p3 = new Person("wangwu", 33);
	//people.add(p1);
	//people.add(p2);
	//people.add(p3);
	//model.addAttribute("singlePerson", single);
	//model.addAttribute("people", people);
	return "index";
  }
  @RequestMapping(value = "/test")
  @ResponseBody
  public Map<String,String> test(){
	Map<String,String> map = new HashMap<>();
	map.put("zhangsan","20");
	map.put("lisi","20");
    return map;
  }

  @RequestMapping(value = "/check")
  @ResponseBody
  public Map<String,String> get(@RequestBody DataSourceRequest param, HttpServletRequest res, HttpServletResponse pon) {
	Map<String,String> map = new HashMap<>();
	map.put("code","1");
	map.put("message","转移数据成功");
	int i = 0;
	try {
	  Connection conn = ConnectionFactory.getConnection(param);
	  if(conn ==null){
		i =2 ;
		throw new RuntimeException();
	  }
	  param.setBooleanTarget(true);
	  try{
		conn = ConnectionFactory.getConnection(param);
	  }catch (Exception e){
		logger.info(e.getMessage());
		i =5;
		throw new RuntimeException(e.getMessage());
	  }
	  if(conn ==null){
		i =1;
		throw new RuntimeException();
	  }
	  param.setBooleanTarget(false);
	  i = 3;
	  if(conn !=null){
		ConnectionFactory.mto(param);
	  }
	}catch (Exception e){
	  map.put("code","2");
	  if(i==1){
		map.put("message","目标数据库连接异常");
	  }else if(i==2){
		map.put("message","源数据库连接异常");
	  }else {
		map.put("message", e.getMessage());
	  }
	}
	return map;
  }
}
