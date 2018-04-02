package com.eas;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.MiniBatchOperationInProgress;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;

public class HBaseCopro001 extends BaseRegionObserver {
	public static final Log LOG = LogFactory.getLog(HRegion.class);

	@Override
	public void preBatchMutate(ObserverContext<RegionCoprocessorEnvironment> c,
			MiniBatchOperationInProgress<Mutation> miniBatchOp)
			throws IOException {

		String tableName = c.getEnvironment().getRegion().getRegionInfo().getTable().getNameAsString();
		LOG.info("Project: PRJ_001 | Table: " + tableName + " | Coprocessor: hdfs:///eas/eas.jar|com.eas.HBaseCoprocessor | Event: prePut | Status: Initialized");
		
		try {		
			Configuration config = HBaseConfiguration.create();
			HTable htlog_001 = new HTable(config, "log_001");

			for (int i = 0; i < miniBatchOp.size(); i++) { 
				Mutation operation = miniBatchOp.getOperation(i); 
				byte[] rkey = operation.getRow();
				long dtrkey = System.currentTimeMillis();
				String newrkey = new String(rkey);
				Put p = null;

				NavigableMap<byte[], List<Cell>> familyCellMap = operation.getFamilyCellMap(); 

				if (operation instanceof Put) {
					newrkey = newrkey + " | P | " + dtrkey;
				} else {
					newrkey = newrkey + " | D | " + dtrkey;
				}
				p = new Put(Bytes.toBytes(newrkey));

				for (Entry<byte[], List<Cell>> entry : familyCellMap.entrySet()) { 

					byte [] cfamily = CellUtil.cloneFamily(entry.getValue().iterator().next());
					Result closestRowBefore = c.getEnvironment().getRegion().getClosestRowBefore(rkey, cfamily);
					
					for (Iterator<Cell> iterator = entry.getValue().iterator(); iterator.hasNext();) { 
						Cell cell = iterator.next(); 
						byte[] cqualifier = CellUtil.cloneQualifier(cell); 
						byte[] cvalue = CellUtil.cloneValue(cell); 

						if (operation instanceof Put){
							if (closestRowBefore != null) {
								byte[] rkeyb = closestRowBefore.getRow();
								
								if (Arrays.equals(rkey, rkeyb)) {
									byte[] cvalueb = closestRowBefore.getValue(cfamily, cqualifier);
									p.addColumn(cfamily, cqualifier, cvalueb);
								}
								else {
									p.addColumn(cfamily, cqualifier, cvalue);
								}								
							}
							else {
								p.addColumn(cfamily, cqualifier, cvalue);								
							}
						}
						else {
							for (Cell c1 : closestRowBefore.rawCells()) {
								byte[] cfamilyb = CellUtil.cloneFamily(c1);
								byte[] cqualifierb = CellUtil.cloneQualifier(c1);
								byte[] cvalueb = CellUtil.cloneValue(c1);
								p.addColumn(cfamilyb, cqualifierb, cvalueb);
							}							
						}

					} 
					htlog_001.put(p);
				} 
			} 
			htlog_001.close();
			LOG.info("Project: PRJ_001 | Table: " + tableName + " | Coprocessor: hdfs:///eas/eas.jar|com.eas.HBaseCoprocessor | Event: prePut | Status: Finalized");

			super.preBatchMutate(c, miniBatchOp);

		} catch (IOException error) {
			LOG.info("Project: PRJ_001 | Table: " + tableName + " | Coprocessor: hdfs:///eas/eas.jar|com.eas.HBaseCoprocessor | Event: prePut | Status: Error");
			error.printStackTrace();
		}
	}
}