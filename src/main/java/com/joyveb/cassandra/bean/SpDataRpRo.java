package com.joyveb.cassandra.bean;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

import org.apache.avro.reflect.Nullable;
import org.springframework.data.annotation.Id;

import com.joyveb.dbpimpl.cass.prepare.mapping.Indexed;
import com.joyveb.dbpimpl.cass.prepare.mapping.Table;

@Data
@Table(name = "t_spdatarpro_ro")
public class SpDataRpRo {
	
	@Id
	private String orderno;
	@Indexed
	private String messageid;
	private String ltype;
	private BigDecimal period;
	private String merchantid;
	@Nullable
	private String merchantuserid;
	@Nullable
	private String content;
	@Nullable
	private BigDecimal amount;
	@Nullable
	private Integer multi;
	@Nullable
	private Integer boards;
	@Nullable
	private String chipintype;
	@Nullable
	private String playtype;
	@Nullable
	private Integer certificatetype;//#证件类型 1:身份证;2:护照;3:军官证
	@Nullable
	private String certificatenum;//#证件号码
	@Nullable
	private String mobile;
	@Nullable
	private String version;
	@Nullable
	private String username;
	@Nullable
	private String serialnum;
	@Nullable
	private Integer batchslicecount;//批次拆单个数
	@Nullable
	private Integer ticketslicecount;//票拆单个数
	@Nullable
	private Integer isoffline;//#是否离线. 0:否,1:是  默认 0
	@Nullable
	private Integer isprev;//#是否预发. 0:否,1:是  默认 0
	@Indexed
	private Integer isvalid = 1;//#是否有效. 0:否,1:是  默认 1
	@Nullable
	private Integer wagerstatus;//#投注状态(0正在出票，1成功，2失败，-2部分成功部分失败，3未投注,落地日金额超上限，4未投注,落地异常超上限)
	@Nullable
	private Integer slicestatus;//#是否已经拆单. 0：否，1：是。 默认：0
	@Nullable
	private Integer notifystatus;//#通知状态(0通知中，1通知成功，2通知失败)
	@Nullable
	private String email;
	@Nullable
	private String fixedregion;// #商户指定落地
	@Nullable
	private Integer proactive;//#是否预发. 0:否,1:是 默认 0
	@Nullable
	private Integer batchstatus;// #批次投注状态(1成功，2失败，-2部分成功部分失败)
	@Nullable
	private Date printtime;
	@Nullable
	private String failreason;
	@Nullable
	private String voids;
	@Nullable
	private Date createdate;
	@Nullable
	private String region;
}
