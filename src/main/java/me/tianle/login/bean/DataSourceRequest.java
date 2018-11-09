package me.tianle.login.bean;

/**
 *
 * @Description: 转化数据库的属性
 *
 * @auther: sheen.lee
 * @date: 13:45 2018/7/28
 * @param:
 * @return:
 *
 */
public class DataSourceRequest {
  private String sourceIp;
  private String targetIp;
  private String sourceUser;
  private String targetUser;
  private String sourcePassword;
  private String targetPassword;
  private String sourceTable;
  private String targetTable;
  private Boolean booleanTarget;//是否目标数据库true表示是，false表示否

  public Boolean getBooleanTarget() {
	return booleanTarget;
  }

  public void setBooleanTarget(Boolean booleanTarget) {
	this.booleanTarget = booleanTarget;
  }

  public String getSourceIp() {
	return sourceIp;
  }

  public void setSourceIp(String sourceIp) {
	this.sourceIp = sourceIp;
  }

  public String getTargetIp() {
	return targetIp;
  }

  public void setTargetIp(String targetIp) {
	this.targetIp = targetIp;
  }

  public String getSourceUser() {
	return sourceUser;
  }

  public void setSourceUser(String sourceUser) {
	this.sourceUser = sourceUser;
  }

  public String getTargetUser() {
	return targetUser;
  }

  public void setTargetUser(String targetUser) {
	this.targetUser = targetUser;
  }

  public String getSourcePassword() {
	return sourcePassword;
  }

  public void setSourcePassword(String sourcePassword) {
	this.sourcePassword = sourcePassword;
  }

  public String getTargetPassword() {
	return targetPassword;
  }

  public void setTargetPassword(String targetPassword) {
	this.targetPassword = targetPassword;
  }

  public String getSourceTable() {
	return sourceTable;
  }

  public void setSourceTable(String sourceTable) {
	this.sourceTable = sourceTable;
  }

  public String getTargetTable() {
	return targetTable;
  }

  public void setTargetTable(String targetTable) {
	this.targetTable = targetTable;
  }
}
