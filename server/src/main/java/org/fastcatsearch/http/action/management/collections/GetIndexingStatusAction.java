package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/collections/indexing-status")
public class GetIndexingStatusAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		responseWriter.object()
		.key("collectionId").value(collectionId);
		
		int sequence = collectionContext.indexStatus().getSequence();
		File indexFileDir = collectionContext.dataFilePaths().indexDirFile(sequence);
		responseWriter.key("sequence").value(sequence);
		String diskSize = "";
		
		if(indexFileDir.exists()){
			long byteCount = FileUtils.sizeOfDirectory(indexFileDir);
			diskSize = FileUtils.byteCountToDisplaySize(byteCount);
		}
		responseWriter.key("diskSize").value(diskSize);
		
		int documentSize = collectionContext.dataInfo().getDocuments();
		responseWriter.key("documentSize").value(documentSize);
		
		String createTime = "";
		SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
		if(segmentInfo != null){
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			if(revisionInfo != null){
				createTime = revisionInfo.getCreateTime();
			}
		}
		responseWriter.key("createTime").value(createTime);
		
		responseWriter.endObject();
		
		responseWriter.done();
	}

}
