window.blockly = window.blockly || {};
window.blockly.js = window.blockly.js || {};
window.blockly.js.blockly = window.blockly.js.blockly || {};
window.blockly.js.blockly.RelatorioAntigo = window.blockly.js.blockly.RelatorioAntigo
		|| {};

/**
 * RelatorioAntigo
 */
window.blockly.js.blockly.RelatorioAntigo.Executar = function() {
	var item;
	this.cronapi.util.openReport('reports/relatorioTeste.jrxml', []);
}
