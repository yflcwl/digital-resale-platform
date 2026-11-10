# 后端全量测试脚本
#
# 说明:warm-flow 第三方库持有静态 Spring 上下文,同一 JVM 内无法反复启停上下文,
# 因此集成测试必须按类分次执行(每个类一个 Maven 进程)。规则引擎单测无此限制。
#
# 用法:pwsh -File script/test-all.ps1
# 前置:PostgreSQL / Redis / RocketMQ 已启动;application.yml 关闭验证码与接口加密。

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

$tests = @(
    @{ Module = 'sm-modules/sm-inspection'; Class = 'GradeRuleEngineTest'; Desc = '规则引擎单测(10)' },
    @{ Module = 'sm-admin'; Class = 'OrderFlowIntegrationTest'; Desc = '订单全链路(10)' },
    @{ Module = 'sm-admin'; Class = 'MessageFlowIntegrationTest'; Desc = '聊天+通知(6)' },
    @{ Module = 'sm-admin'; Class = 'AdminFlowIntegrationTest'; Desc = '管理后台(6)' },
    @{ Module = 'sm-admin'; Class = 'InspectionFlowIntegrationTest'; Desc = '智能质检(5)' }
)

$failed = @()
foreach ($t in $tests) {
    Write-Output "===== $($t.Desc): $($t.Class) ====="
    if ($t.Module -eq 'sm-admin') {
        & mvn -q -f "$root\pom.xml" -pl sm-admin -am test "-DskipTests=false" "-Dtest=$($t.Class)" "-Dsurefire.failIfNoSpecifiedTests=false"
    } else {
        & mvn -q -f "$root\pom.xml" -pl $t.Module -am test "-DskipTests=false" "-Dtest=$($t.Class)" "-Dsurefire.failIfNoSpecifiedTests=false"
    }
    if ($LASTEXITCODE -ne 0) {
        $failed += $t.Class
    }
}

Write-Output ''
if ($failed.Count -eq 0) {
    Write-Output 'ALL TESTS PASSED'
} else {
    Write-Output "FAILED: $($failed -join ', ')"
    exit 1
}
