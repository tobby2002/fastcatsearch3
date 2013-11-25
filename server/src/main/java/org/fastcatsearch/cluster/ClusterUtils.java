package org.fastcatsearch.cluster;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ClusterUtils.class);
	
	public static boolean[] sendJobToNodeList(Job job, NodeService nodeService, List<Node> nodeList, boolean includeMyNode) {
		
		boolean[] resultList = new boolean[nodeList.size()];
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if(!includeMyNode && nodeService.isMyNode(node)){
				//자신에게는 실행요청하지 않음.
				resultFutureList.add(null);
				continue;
			}
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			if(resultFuture == null){
				//네트워크 장애 등으로 전송실패.
				logger.debug("{} 으로 전송하지 못햇습니다. ", nodeList.get(i));
			}
			resultFutureList.add(resultFuture);
		}
		for (int i = 0; i < resultFutureList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = resultFutureList.get(i);
			if(resultFuture != null){
				Object obj = resultFuture.take();
				if(resultFuture.isSuccess()){
					resultList[i] = true;
				}else{
					logger.debug("[{}] job 결과 : {}", node, obj);
				}
			}
		}
		return resultList;
	}
}
