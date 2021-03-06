package org.mermaid.pilog.agent

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.utility.JavaModule
import org.mermaid.pilog.agent.advice.SpringWebAdvice
import org.mermaid.pilog.agent.common.blockingQueue
import org.mermaid.pilog.agent.handler.loadHandler
import org.mermaid.pilog.agent.plugin.factory.loadPlugin
import org.mermaid.pilog.agent.plugin.factory.pluginGroup
import java.lang.instrument.Instrumentation

/**
 * description: vm参数中添加：-javaagent:${user.home}\mermaid-pilot-agent\target\mermaid-pilot-agent-1.0-jar-with-dependencies.jar
 * copyright: Copyright (c) 2018-2021
 * company: iSysCore Tech. Co., Ltd.
 * @author 78721
 * @date 2021/2/1011:40
 * @version 1.0
 */
class PilotAgent {
    companion object {
        @JvmStatic
        fun premain(args: String?, inst: Instrumentation) {
            println("基于javaagent链路跟踪PIVOT信息收集器")
            println("=================================================================")
            loadPlugin()
            loadHandler()
            initialize()
            var agentBuilder : AgentBuilder = AgentBuilder.Default().with(builderListener()).disableClassFormatChanges()
                    .ignore(ElementMatchers.none<TypeDescription>().and(ElementMatchers.nameStartsWith<TypeDescription>("main")))
            pluginGroup.forEach { p -> p.buildInterceptPoint().forEach { agentBuilder = agentBuilder.type(notMatcher().and(it.buildTypesMatcher())).transform { builder, _, _, _ -> builder.visit(Advice.to(p.interceptorAdviceClass()).on(ElementMatchers.not(ElementMatchers.isConstructor()).and(it.buildMethodsMatcher()))) } } }
            agentBuilder.installOn(inst)

        }

        private fun notMatcher(): ElementMatcher.Junction<TypeDescription>  = ElementMatchers.not(ElementMatchers.nameContains("intellij"))

        private fun builderListener(): AgentBuilder.Listener? = object : AgentBuilder.Listener {
            override fun onDiscovery(p0: String?, p1: ClassLoader?, p2: JavaModule?, p3: Boolean) {
                //TODO("Not yet implemented")
            }

            override fun onTransformation(typeDescription: TypeDescription?, p1: ClassLoader?, p2: JavaModule?, p3: Boolean, p4: DynamicType?) {
                org.mermaid.pilog.agent.plugin.factory.logger.info("onTransformation:$typeDescription,dynamicType:$p4")
            }

            override fun onIgnored(p0: TypeDescription?, p1: ClassLoader?, p2: JavaModule?, p3: Boolean) {
                //TODO("Not yet implemented")
            }

            override fun onError(p0: String?, p1: ClassLoader?, p2: JavaModule?, p3: Boolean, p4: Throwable?) {
                org.mermaid.pilog.agent.plugin.factory.logger.warning("方法执行异常,class is $p0,classLoader is $p1,Throwable:\n $p4")
            }

            override fun onComplete(p0: String?, p1: ClassLoader?, p2: JavaModule?, p3: Boolean) {
                //("Not yet implemented")
            }
        }

        private fun initialize() {
            Thread {
                while (true) {
                    if (blockingQueue.isNotEmpty()) {
                        //将span信息推送到服务端
                        blockingQueue.take().run {
                            println("将信息推送到服务端：${toString()}")
                        }
                    } else {
                        Thread.sleep(1000)
                    }
                }
            }.start()
        }
    }
}