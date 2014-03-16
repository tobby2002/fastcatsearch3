package org.fastcatsearch.http.action.management.collections;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.util.JAXBConfigs;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/update-datasource", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateCollectionDatasourceAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		String message = "";
		try {
		
			Pattern keyPattern = Pattern.compile("^key([0-9]+)$");
			Set<String> keySet = request.getParameterMap().keySet();
			
			Map<String,String> properties = new HashMap<String,String>();
			
			logger.trace("parameters:{}", properties);
			
			for(String key : keySet) {
				Matcher matcher = keyPattern.matcher(key);
				if(matcher.find()) {
					int inx = Integer.parseInt(matcher.group(1));
					properties.put( request.getParameter(key),
							request.getParameter("value"+inx));
				}
			}
			
			String collectionId = request.getParameter("collectionId");
			int sourceIndex = request.getIntParameter("sourceIndex",-1);
			
			String indexType = request.getParameter("indexType");
			String name = request.getParameter("name");
			boolean active = "true".equals(request.getParameter("active"));
			String readerClass = request.getParameter("readerClass");
			String modifierClass = request.getParameter("modifierClass");
			String mode = request.getParameter("mode");
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			
			
			File dataSourceConfigFile = collectionContext.collectionFilePaths().file(SettingFileNames.datasourceConfig);
			
			List<SingleSourceConfig> configList = null;
			SingleSourceConfig config = null;
			
			if("full".equals(indexType)) {
				configList = dataSourceConfig.getFullIndexingSourceConfig();
			} else if("add".equals(indexType)) {
				configList = dataSourceConfig.getAddIndexingSourceConfig();
			}
			
			
			if("delete".equals(mode)) {
				if(configList != null && sourceIndex != -1){
					configList.remove(sourceIndex);
					isSuccess = true;
				}else{
					isSuccess = false;
					message = "Cannot find config to delete. sourceIndex[" + sourceIndex + "] configList size[" + (configList == null ? 0 : configList.size()) + "]";
				}
			} else {
				
				if (configList == null) {
					configList = new ArrayList<SingleSourceConfig>();
					if ("full".equals(indexType)) {
						dataSourceConfig.setFullIndexingSourceConfig(configList);
					} else if ("add".equals(indexType)) {
						dataSourceConfig.setAddIndexingSourceConfig(configList);
					}
				}
				
				if(sourceIndex == -1 || configList.size() == 0) {
					config = new SingleSourceConfig();
					configList.add(config);
				} else if(sourceIndex < configList.size()) {
					config = configList.get(sourceIndex);
				} else {
					message = "Invalid request. sourceIndex[" + sourceIndex + "] configList size[" + (configList == null ? 0 : configList.size()) + "]";
					isSuccess = false;
				}
				
				if (config != null) {
					config.setName(name);
					config.setActive(active);
					config.setSourceReader(readerClass);
					config.setSourceModifier(modifierClass);
					config.setProperties(properties);
					
					isSuccess = true;
				}else{
					isSuccess = false;
				}
			}
		
			if(isSuccess) {
				JAXBConfigs.writeConfig(dataSourceConfigFile, dataSourceConfig, DataSourceConfig.class);
			}
			
		} catch (Exception e) {
			logger.error("",e);
			isSuccess = false;
		} finally {
			ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
			responseWriter.object();
			responseWriter.key("success").value(isSuccess);
			responseWriter.key("message").value(message);
			responseWriter.endObject();
			responseWriter.done();
		}
	}
}
