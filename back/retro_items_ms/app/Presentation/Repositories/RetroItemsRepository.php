<?php

namespace App\Presentation\Repositories;

use App\Controllers\RetroItemsController;
use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;

class RetroItemsRepository
{
    public function get_all_items(Request $req, Response $resp, $args): Response
    {
        $controller = new RetroItemsController();
        $rows = $controller->all();
        if (count($rows) == 0) {
            return $resp->withStatus(204);
        }
        $resp->getBody()->write($rows->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function get_items_by_sprint(Request $req, Response $resp, $args): Response
    {
        $sprintId = (int) $args['sprint_id'];
        $controller = new RetroItemsController();
        $rows = $controller->find_by_sprint($sprintId);
        if (count($rows) == 0) {
            return $resp->withStatus(204);
        }
        $resp->getBody()->write($rows->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function get_item(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args['id'];
        $controller = new RetroItemsController();
        $row = $controller->find_by_id($id);
        if (empty($row)) {
            return $resp->withStatus(404);
        }
        $resp->getBody()->write($row->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function create_item(Request $req, Response $resp, $args): Response
    {
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        if (empty($data['sprint_id']) || empty($data['descripcion'])) {
            return $resp->withStatus(400);
        }

        $controller = new RetroItemsController();
        $row = $controller->create($data);
        $resp->getBody()->write($row->toJson());
        return $resp
            ->withStatus(201)
            ->withHeader('Content-Type', 'application/json');
    }

    public function update_item(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args['id'];
        $body = $req->getBody()->getContents();
        $data = json_decode($body, true);

        $controller = new RetroItemsController();
        $row = $controller->update($id, $data ?? []);
        if (empty($row)) {
            return $resp->withStatus(404);
        }
        $resp->getBody()->write($row->toJson());
        return $resp
            ->withStatus(200)
            ->withHeader('Content-Type', 'application/json');
    }

    public function delete_item(Request $req, Response $resp, $args): Response
    {
        $id = (int) $args['id'];
        $controller = new RetroItemsController();
        $row = $controller->delete($id);
        if (empty($row)) {
            return $resp->withStatus(404);
        }
        return $resp->withStatus(200);
    }
}