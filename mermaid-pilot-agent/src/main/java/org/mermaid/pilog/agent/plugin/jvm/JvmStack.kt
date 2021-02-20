package org.mermaid.pilog.agent.plugin.jvm

import java.lang.management.ManagementFactory
import java.util.*

/**
 * description: TODO
 * copyright: Copyright (c) 2018-2021
 * company: iSysCore Tech. Co., Ltd.
 * @author 库陈胜
 * @date 2021/2/1818:20
 * @version 1.0
 */
fun printMemoryInfo() {
    val memory = ManagementFactory.getMemoryMXBean()
    val headMemory = memory.heapMemoryUsage

    val info = "\ninit:${headMemory.init}MB\tmax:${headMemory.max}MB\tused:${headMemory.used}MB\tcommited:${headMemory.committed}MB\tuse rate:${headMemory.used * 100 / headMemory.committed}%"
    print(info)

    val nonHeadMemory = memory.nonHeapMemoryUsage
    val nInfo = "init:${nonHeadMemory.init}MB\tmax:${nonHeadMemory.max}MB\tused:${nonHeadMemory.used}MB\tcommited:${nonHeadMemory.committed}MB\tuse rate:${nonHeadMemory.used * 100 / headMemory.committed}%"
    println(nInfo)
}

fun printGCInfo() {
    ManagementFactory.getGarbageCollectorMXBeans().forEach {
        "name:${it.name}\tcount:${it.collectionCount}\ttook:${it.collectionTime}\tpool name:${Arrays.deepToString(it.memoryPoolNames)}".run { println(this) }
    }

}