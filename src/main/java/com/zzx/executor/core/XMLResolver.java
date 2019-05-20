package com.zzx.executor.core;

import com.zzx.executor.entity.TradeEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
public class XMLResolver extends AbstractXMLResolver {

    @Override
    public List<TradeEntity> messageResolve(String xmlData) {

        Pattern pattern = Pattern.compile("(.*?)o-o-t-l-content(?<datanum>.*)<thead>(.*?)");
        Matcher matcher = pattern.matcher(xmlData);
        String account = null;
        String datanum = null;
        if (matcher.find()) {
            datanum = matcher.group("datanum");
            System.out.println(datanum);
        }

        String result = null;
        if (datanum != null) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = datanum;
            result = result.replaceAll("(?i)<td(.*?)>", "<td>"); //(?i)忽略大小写，(.*?)饿汉模式，清空tr中的属性
            result = result.replaceAll("(?i)<tr(.*?)>", "<tr>");
            result = result.replaceAll("(?i)</tr>", "</tr>"); //(?i)忽略大小写，(.*?)饿汉模式，清空tr中的属性
            result = result.replaceAll("(?i)</td>", "</td>");
            result = result.replaceAll("<thead(.*?)</thead>", "");
            result = result.replaceAll("<iclass(.*?)</i>", "");
            result = result.replaceAll("<p(.*?)</p>", "");
            result = result.replaceAll("<em(.*?)</em>", "");
            result = result.replaceAll("<div(.*?)>", "");
            result = result.replaceAll("</div>", "");
            result = result.replaceAll("<span(.*?)>", "");
            result = result.replaceAll("<a(.*?)>", "");
            result = result.replaceAll("<a(.*?)>", "");
            result = result.replaceAll("<td>查看(.*?)联系买家</a></td>", "");
            result = result.replaceAll("<ul(.*?)>", "<tr>");
            result = result.replaceAll("</ul>", "</tr>");
            result = result.replaceAll("<li(.*?)>", "<td>");
            result = result.replaceAll("</li>", "</td>");

            result = result.replaceAll("&nbsp;", "");
            result = result.replaceAll("<table>", "");
            result = result.replaceAll("<tbody(.*?)>", "");
            result = result.replaceAll("</table>", "");
            result = result.replaceAll("<tbody>", "");
            result = result.replaceAll("</span>", "");
            result = result.replaceAll("<!--(.*?)-->", "");
            result = result.replaceAll("</tbody>", "");
            result = result.replaceAll("</tr><tableclass=pdd-dui-table><tr>", "");
            result = result.replaceAll("<tableclass=pdd-dui-table>", "");
            result = result.replaceAll("<br/>", "");
            result = result.replaceAll("</a>", "");
            result=result.substring(result.indexOf("<tr>"));

            result = "<div>" + result + "</div>";

        }
        if (result == null) {
            return new ArrayList<>();
        }



        try {
            Document document = DocumentHelper.parseText(result);
            org.dom4j.Element root = document.getRootElement();
            List<TradeEntity> records = new ArrayList<>();
            Iterator var = root.elementIterator();//xml形态的记录列表
//            int i = 0;

            while (var.hasNext()) {
                //跳过表头，从第1行开始读数据(农行不需要)
//                if (i == 0) {
//                    var.next();
//                    i++;
//                    continue;
//                }
                TradeEntity record = new TradeEntity();
                org.dom4j.Element recordRoot = (org.dom4j.Element) var.next();
                Iterator var2 = recordRoot.elementIterator();

                if (var2.hasNext()) {
                    //备注-订单号
                    String item1 = ((org.dom4j.Element) var2.next()).getText();
                    Pattern patternorder=Pattern.compile("(.*?)(?<orders>\\d+)-(?<orderss>\\d+)定金");
                    Matcher matcherorder=patternorder.matcher(item1);
                    if(matcherorder.find()){
                       String order1= matcherorder.group("orders");
                       String order2= matcherorder.group("orderss");
                       record.setBankAccount(order1+"-"+order2);
                       record.setRemark(order1+"-"+order2);
                    }

                }
                // 时间
                if (var2.hasNext()) {
                    String item6 = ((org.dom4j.Element) var2.next()).getText();
                    item6=item6.substring(7);
                    String date=item6.substring(0,10);
                    String time=item6.substring(10);
                    record.setTradeDate(date);
                    record.setTradeTime(time);
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                if (var2.hasNext()) {
                    var2.next();
                }
                //交易金额，带"-"的支出不要
                if (var2.hasNext()) {
                    String item4 = ((org.dom4j.Element) var2.next()).getText();
                    if (StringUtils.isEmpty(item4) || item4.contains("-")) {
                        continue; //下一行
                    }
                    item4 = item4.replace("+", "");
                    record.setMoney(item4);
                }
                //类型
                if (var2.hasNext()) {
                    record.setTradeType("拼多多收款");
                }

                 //类型
                record.setBank("固定金额");   //账号后4位

                record.setReport(false);  //已上报=false
                System.out.println("************:"+record.toString() +"************");
                //如果这条信息已存在，说明后面的都已经解析过，不再解析
                if (dataProcessor.containsRecord(record)) {
                    break;
                }
                records.add(record);
            }

            return records;
        } catch (DocumentException e) {

            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
