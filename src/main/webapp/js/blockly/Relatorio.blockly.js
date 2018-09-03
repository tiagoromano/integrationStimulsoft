window.blockly = window.blockly || {};
window.blockly.js = window.blockly.js || {};
window.blockly.js.blockly = window.blockly.js.blockly || {};
window.blockly.js.blockly.Relatorio = window.blockly.js.blockly.Relatorio || {};

/**
 * Relatorio
 */
window.blockly.js.blockly.Relatorio.Executar = function() {
	var item;
	this.cronapi.util.openReport('reports/novo.report', []);
}
