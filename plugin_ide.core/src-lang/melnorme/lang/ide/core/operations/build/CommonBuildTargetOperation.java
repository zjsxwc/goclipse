/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.ide.core.operations.build;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;

import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import melnorme.lang.ide.core.launch.LaunchUtils;
import melnorme.lang.ide.core.operations.AbstractToolManagerOperation;
import melnorme.lang.ide.core.operations.OperationInfo;
import melnorme.utilbox.collections.ArrayList2;
import melnorme.utilbox.concurrency.OperationCancellation;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

public abstract class CommonBuildTargetOperation extends AbstractToolManagerOperation {
	
	protected final BuildManager buildManager;
	protected final OperationInfo opInfo;
	protected final BuildTargetValidator3 buildTargetValidator;
	protected final Path buildToolPath;
	protected final boolean fullBuild;
	
	public CommonBuildTargetOperation(BuildManager buildManager, BuildTargetValidator3 buildTargetValidator, 
			OperationInfo opInfo, Path buildToolPath, boolean fullBuild) {
		super(assertNotNull(buildTargetValidator).getProject());
		this.buildManager = assertNotNull(buildManager);
		this.buildToolPath = buildToolPath;
		this.fullBuild = fullBuild;
		this.opInfo = assertNotNull(opInfo);
		this.buildTargetValidator = assertNotNull(buildTargetValidator);
	}
	
	protected Path getBuildToolPath() throws CommonException {
		return buildToolPath;
	}
	
	protected String getEffectiveBuildOptions() throws CommonException, CoreException {
		return buildTargetValidator.getEffectiveBuildOptions();
	}
	
	protected String[] getEvaluatedAndParserArguments() throws CoreException, CommonException {
		return LaunchUtils.getEvaluatedAndParsedArguments(getEffectiveBuildOptions());
	}
	
	@Override
	public void execute(IProgressMonitor pm) throws CoreException, CommonException, OperationCancellation {
		ProcessBuilder pb = getToolProcessBuilder();
		ExternalProcessResult processResult = runBuildTool(opInfo, pb, pm);
		processBuildOutput(processResult);
	}
	
	protected ProcessBuilder getToolProcessBuilder() throws CoreException, CommonException, OperationCancellation {
		ArrayList2<String> commands = new ArrayList2<String>();
		addToolCommand(commands);
		
		addMainArguments(commands);
		
		commands.addElements(getEvaluatedAndParserArguments());
		return getProcessBuilder(commands);
	}
	
	protected void addToolCommand(ArrayList2<String> commands) 
			throws CoreException, CommonException, OperationCancellation {
		commands.add(getBuildToolPath().toString());
	}
	
	protected abstract void addMainArguments(ArrayList2<String> commands)
			throws CoreException, CommonException, OperationCancellation;
	
	protected abstract ProcessBuilder getProcessBuilder(ArrayList2<String> commands) 
			throws CommonException, OperationCancellation, CoreException;
	
	protected abstract void processBuildOutput(ExternalProcessResult processResult)
			throws CoreException, CommonException, OperationCancellation;
			
}